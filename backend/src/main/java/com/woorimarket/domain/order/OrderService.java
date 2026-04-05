package com.woorimarket.domain.order;

import com.woorimarket.domain.notification.NotificationService;
import com.woorimarket.domain.order.dto.OrderRequest;
import com.woorimarket.domain.order.dto.OrderResponse;
import com.woorimarket.domain.payment.PaymentService;
import com.woorimarket.domain.product.ProductInventory;
import com.woorimarket.domain.product.ProductInventoryRepository;
import com.woorimarket.domain.store.Store;
import com.woorimarket.domain.store.StoreRepository;
import com.woorimarket.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductInventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public OrderResponse createOrder(User buyer, OrderRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        Order order = Order.builder()
                .buyer(buyer)
                .store(store)
                .orderDate(request.getOrderDate())
                .totalAmount(BigDecimal.ZERO)
                .status(Order.OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            ProductInventory inventory = inventoryRepository.findById(itemReq.getInventoryId())
                    .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. ID: " + itemReq.getInventoryId()));

            if (!inventory.canOrder(itemReq.getQuantity())) {
                throw new IllegalStateException(
                        inventory.getProduct().getName() + "의 재고가 부족합니다. (남은 수량: " + inventory.getRemainingQuantity() + ")");
            }

            // 재고 차감
            inventory.decreaseStock(itemReq.getQuantity());
            inventoryRepository.save(inventory);

            OrderItem orderItem = OrderItem.builder()
                    .productInventory(inventory)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(inventory.getPrice())
                    .subtotal(inventory.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();

            order.addItem(orderItem);
        }

        order.calculateTotal();
        order = orderRepository.save(order);

        // Mock 결제 처리
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD";
        paymentService.processPayment(order, paymentMethod);

        // 주문 확인으로 상태 변경
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order = orderRepository.save(order);

        // 알림 발송
        notificationService.sendOrderNotification(buyer, store.getUser(), order);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerOrders(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 구매자 또는 판매자만 조회 가능
        if (!order.getBuyer().getId().equals(userId) && !order.getStore().getUser().getId().equals(userId)) {
            throw new IllegalStateException("조회 권한이 없습니다.");
        }

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerOrders(Long sellerId, LocalDate date) {
        Store store = storeRepository.findByUserId(sellerId)
                .orElseThrow(() -> new IllegalStateException("매장 정보가 없습니다."));

        List<Order> orders;
        if (date != null) {
            orders = orderRepository.findByStoreIdAndOrderDate(store.getId(), date);
        } else {
            orders = orderRepository.findByStoreIdOrderByCreatedAtDesc(store.getId());
        }

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSellerStats(Long sellerId, LocalDate startDate, LocalDate endDate) {
        Store store = storeRepository.findByUserId(sellerId)
                .orElseThrow(() -> new IllegalStateException("매장 정보가 없습니다."));

        Long storeId = store.getId();

        Map<String, Object> stats = new HashMap<>();

        // 총 매출
        BigDecimal totalRevenue = orderRepository.sumTotalByStoreAndDateRange(storeId, startDate, endDate);
        stats.put("totalRevenue", totalRevenue);

        // 기간 내 주문 목록
        List<Order> orders = orderRepository.findByStoreAndDateRange(storeId, startDate, endDate);
        stats.put("totalOrders", orders.size());

        // 일별 매출
        Map<LocalDate, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        Map<LocalDate, Long> dailyOrderCount = new LinkedHashMap<>();

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dailyRevenue.put(d, BigDecimal.ZERO);
            dailyOrderCount.put(d, 0L);
        }

        for (Order order : orders) {
            if (order.getStatus() != Order.OrderStatus.CANCELLED) {
                dailyRevenue.merge(order.getOrderDate(), order.getTotalAmount(), BigDecimal::add);
                dailyOrderCount.merge(order.getOrderDate(), 1L, Long::sum);
            }
        }

        stats.put("dailyRevenue", dailyRevenue);
        stats.put("dailyOrderCount", dailyOrderCount);

        // 상품별 판매량
        Map<String, Integer> productSales = new HashMap<>();
        for (Order order : orders) {
            if (order.getStatus() != Order.OrderStatus.CANCELLED) {
                for (OrderItem item : order.getItems()) {
                    String productName = item.getProductInventory().getProduct().getName();
                    productSales.merge(productName, item.getQuantity(), Integer::sum);
                }
            }
        }
        stats.put("productSales", productSales);

        return stats;
    }
}
