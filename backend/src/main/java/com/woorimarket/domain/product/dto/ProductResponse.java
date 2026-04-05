package com.woorimarket.domain.product.dto;

import com.woorimarket.domain.product.Product;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String unit;
    private String imageUrl;
    private String category;
    private Boolean active;
    private Long storeId;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .unit(product.getUnit())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .active(product.getActive())
                .storeId(product.getStore().getId())
                .build();
    }
}
