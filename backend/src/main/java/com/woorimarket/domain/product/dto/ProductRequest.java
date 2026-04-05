package com.woorimarket.domain.product.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ProductRequest {
    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    private String description;

    @NotBlank(message = "단위는 필수입니다.")
    private String unit;

    private String imageUrl;
    private String category;
}
