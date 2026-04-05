package com.woorimarket.domain.admin;

import com.woorimarket.domain.order.OrderService;
import com.woorimarket.domain.store.Store;
import com.woorimarket.domain.store.StoreRepository;
import com.woorimarket.domain.store.dto.StoreResponse;
import com.woorimarket.domain.user.User;
import com.woorimarket.domain.user.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderService orderService;

    @GetMapping("/sellers")
    public ResponseEntity<List<SellerInfo>> getSellers() {
        List<User> sellers = userRepository.findByRole(User.Role.SELLER);
        List<SellerInfo> result = new ArrayList<>();

        for (User seller : sellers) {
            Optional<Store> store = storeRepository.findByUserId(seller.getId());
            result.add(SellerInfo.builder()
                    .id(seller.getId())
                    .name(seller.getName())
                    .email(seller.getEmail())
                    .phone(seller.getPhone())
                    .createdAt(seller.getCreatedAt().toString())
                    .storeId(store.map(Store::getId).orElse(null))
                    .storeName(store.map(Store::getStoreName).orElse(null))
                    .storeActive(store.map(Store::getActive).orElse(null))
                    .build());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/sellers/{sellerId}/stats")
    public ResponseEntity<Map<String, Object>> getSellerStats(
            @PathVariable Long sellerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(orderService.getSellerStats(sellerId, startDate, endDate));
    }

    @GetMapping("/sellers/{sellerId}/store")
    public ResponseEntity<StoreResponse> getSellerStore(@PathVariable Long sellerId) {
        Store store = storeRepository.findByUserId(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 판매자의 매장이 없습니다."));
        return ResponseEntity.ok(StoreResponse.from(store));
    }

    @Data
    @Builder
    public static class SellerInfo {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String createdAt;
        private Long storeId;
        private String storeName;
        private Boolean storeActive;
    }
}
