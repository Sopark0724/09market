package com.woorimarket.domain.store;

import com.woorimarket.domain.order.OrderService;
import com.woorimarket.domain.order.dto.OrderResponse;
import com.woorimarket.domain.product.ProductService;
import com.woorimarket.domain.product.dto.*;
import com.woorimarket.domain.store.dto.StoreRequest;
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
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final StoreService storeService;
    private final ProductService productService;
    private final OrderService orderService;

    // === 매장 관리 ===

    @PostMapping("/store")
    public ResponseEntity<StoreResponse> createOrUpdateStore(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid StoreRequest request) {
        return ResponseEntity.ok(storeService.createOrUpdate(user, request));
    }

    @GetMapping("/store")
    public ResponseEntity<StoreResponse> getMyStore(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(storeService.getMyStore(user.getId()));
    }

    // === 상품 관리 ===

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(user.getId(), request));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(user.getId(), id, request));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getMyProducts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.getMyProducts(user.getId()));
    }

    // === 재고 관리 ===

    @PostMapping("/inventory")
    public ResponseEntity<List<InventoryResponse>> createInventoryBulk(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid InventoryBulkRequest request) {
        return ResponseEntity.ok(productService.createInventoryBulk(user.getId(), request));
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody @Valid InventoryRequest request) {
        return ResponseEntity.ok(productService.updateInventory(user.getId(), id, request));
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryResponse>> getInventoryByDate(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(productService.getInventoryByDate(user.getId(), date));
    }

    // === 주문 현황 ===

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String phone) {
        return ResponseEntity.ok(orderService.getSellerOrders(user.getId(), date, phone));
    }

    @PutMapping("/orders/{id}/complete")
    public ResponseEntity<OrderResponse> completeOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.completeOrder(id, user.getId()));
    }

    // === 통계 ===

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(orderService.getSellerStats(user.getId(), startDate, endDate));
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(orderService.getSellerDailyStats(user.getId(), date));
    }
}
