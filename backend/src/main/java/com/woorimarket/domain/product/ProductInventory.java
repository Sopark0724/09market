package com.woorimarket.domain.product;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_inventories",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "available_date"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer remainingQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = InventoryStatus.AVAILABLE;
        if (remainingQuantity == null) remainingQuantity = stockQuantity;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (remainingQuantity != null && remainingQuantity <= 0) {
            status = InventoryStatus.SOLD_OUT;
        }
    }

    public boolean canOrder(int quantity) {
        return status == InventoryStatus.AVAILABLE && remainingQuantity >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (!canOrder(quantity)) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.remainingQuantity -= quantity;
        if (this.remainingQuantity <= 0) {
            this.status = InventoryStatus.SOLD_OUT;
        }
    }

    public enum InventoryStatus {
        AVAILABLE, SOLD_OUT, CLOSED
    }
}
