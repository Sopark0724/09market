package com.woorimarket.domain.store.dto;

import com.woorimarket.domain.store.Store;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreResponse {
    private Long id;
    private String storeName;
    private String address;
    private String addressDetail;
    private String phone;
    private String businessHours;
    private String description;
    private String imageUrl;
    private Boolean active;
    private String sellerName;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .addressDetail(store.getAddressDetail())
                .phone(store.getPhone())
                .businessHours(store.getBusinessHours())
                .description(store.getDescription())
                .imageUrl(store.getImageUrl())
                .active(store.getActive())
                .sellerName(store.getUser().getName())
                .build();
    }
}
