package com.woorimarket.domain.store.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class StoreRequest {
    @NotBlank(message = "매장명은 필수입니다.")
    private String storeName;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    private String addressDetail;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    private String businessHours;
    private String description;
    private String imageUrl;
}
