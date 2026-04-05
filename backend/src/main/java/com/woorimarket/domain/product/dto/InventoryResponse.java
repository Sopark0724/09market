package com.woorimarket.domain.product.dto;

import com.woorimarket.domain.product.ProductInventory;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class InventoryResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String unit;
    private String category;
    private String imageUrl;
    private String detailImages;
    private String description;
    private LocalDate availableDate;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer remainingQuantity;
    private String status;

    public static InventoryResponse from(ProductInventory inv) {
        return InventoryResponse.builder()
                .id(inv.getId())
                .productId(inv.getProduct().getId())
                .productName(inv.getProduct().getName())
                .unit(inv.getProduct().getUnit())
                .category(inv.getProduct().getCategory())
                .imageUrl(inv.getProduct().getImageUrl())
                .detailImages(inv.getProduct().getDetailImages())
                .description(inv.getProduct().getDescription())
                .availableDate(inv.getAvailableDate())
                .price(inv.getPrice())
                .stockQuantity(inv.getStockQuantity())
                .remainingQuantity(inv.getRemainingQuantity())
                .status(inv.getStatus().name())
                .build();
    }
}
