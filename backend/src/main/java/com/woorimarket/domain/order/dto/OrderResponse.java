package com.woorimarket.domain.order.dto;

import com.woorimarket.domain.order.Order;
import com.woorimarket.domain.order.OrderItem;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long buyerId;
    private String buyerName;
    private String buyerPhone;
    private Long storeId;
    private String storeName;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long inventoryId;
        private String productName;
        private String unit;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyer().getId())
                .buyerName(order.getBuyer().getName())
                .buyerPhone(order.getBuyer().getPhone())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getStoreName())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(OrderResponse::mapItem)
                        .collect(Collectors.toList()))
                .build();
    }

    private static OrderItemResponse mapItem(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .inventoryId(item.getProductInventory().getId())
                .productName(item.getProductInventory().getProduct().getName())
                .unit(item.getProductInventory().getProduct().getUnit())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
