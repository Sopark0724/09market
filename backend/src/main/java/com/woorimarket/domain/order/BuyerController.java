package com.woorimarket.domain.order;

import com.woorimarket.domain.order.dto.OrderRequest;
import com.woorimarket.domain.order.dto.OrderResponse;
import com.woorimarket.domain.product.ProductService;
import com.woorimarket.domain.product.dto.InventoryResponse;
import com.woorimarket.domain.store.StoreService;
import com.woorimarket.domain.store.dto.StoreResponse;
import com.woorimarket.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final StoreService storeService;
    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping("/stores")
    public ResponseEntity<List<StoreResponse>> getStores() {
        return ResponseEntity.ok(storeService.getActiveStores());
    }

    @GetMapping("/stores/{storeId}")
    public ResponseEntity<StoreResponse> getStoreDetail(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.getStoreById(storeId));
    }

    @GetMapping("/stores/{storeId}/dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(@PathVariable Long storeId) {
        return ResponseEntity.ok(productService.getAvailableDates(storeId));
    }

    @GetMapping("/stores/{storeId}/inventory")
    public ResponseEntity<List<InventoryResponse>> getStoreInventory(
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(productService.getAvailableInventory(storeId, date));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(user, request));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getBuyerOrders(user.getId()));
    }

    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, user.getId()));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id, user.getId()));
    }
}
