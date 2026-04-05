package com.woorimarket.domain.product;

import com.woorimarket.domain.product.dto.*;
import com.woorimarket.domain.store.Store;
import com.woorimarket.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductInventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;

    public ProductResponse createProduct(Long userId, ProductRequest request) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("먼저 매장을 등록해주세요."));

        Product product = Product.builder()
                .store(store)
                .name(request.getName())
                .description(request.getDescription())
                .unit(request.getUnit())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .active(true)
                .build();

        product = productRepository.save(product);
        return ProductResponse.from(product);
    }

    public ProductResponse updateProduct(Long userId, Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (!product.getStore().getUser().getId().equals(userId)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());

        product = productRepository.save(product);
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getMyProducts(Long userId) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("매장 정보가 없습니다."));
        return productRepository.findByStoreId(store.getId()).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> createInventoryBulk(Long userId, InventoryBulkRequest request) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("매장 정보가 없습니다."));

        List<ProductInventory> inventories = new ArrayList<>();

        for (InventoryRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 ID " + item.getProductId() + "를 찾을 수 없습니다."));

            if (!product.getStore().getId().equals(store.getId())) {
                throw new IllegalStateException("해당 상품에 대한 권한이 없습니다.");
            }

            // 기존 동일 날짜 재고가 있으면 업데이트
            List<ProductInventory> existing = inventoryRepository
                    .findByProductIdAndAvailableDate(item.getProductId(), item.getAvailableDate());

            ProductInventory inventory;
            if (!existing.isEmpty()) {
                inventory = existing.get(0);
                inventory.setPrice(item.getPrice());
                inventory.setStockQuantity(item.getStockQuantity());
                inventory.setRemainingQuantity(item.getStockQuantity());
                inventory.setStatus(ProductInventory.InventoryStatus.AVAILABLE);
            } else {
                inventory = ProductInventory.builder()
                        .product(product)
                        .availableDate(item.getAvailableDate())
                        .price(item.getPrice())
                        .stockQuantity(item.getStockQuantity())
                        .remainingQuantity(item.getStockQuantity())
                        .status(ProductInventory.InventoryStatus.AVAILABLE)
                        .build();
            }

            inventories.add(inventoryRepository.save(inventory));
        }

        return inventories.stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    public InventoryResponse updateInventory(Long userId, Long inventoryId, InventoryRequest request) {
        ProductInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다."));

        if (!inventory.getProduct().getStore().getUser().getId().equals(userId)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        inventory.setPrice(request.getPrice());
        inventory.setStockQuantity(request.getStockQuantity());
        inventory.setRemainingQuantity(request.getStockQuantity());
        inventory.setStatus(ProductInventory.InventoryStatus.AVAILABLE);

        inventory = inventoryRepository.save(inventory);
        return InventoryResponse.from(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByDate(Long userId, LocalDate date) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("매장 정보가 없습니다."));

        LocalDate endDate = (date != null) ? date : LocalDate.now().plusDays(30);
        LocalDate startDate = (date != null) ? date : LocalDate.now();

        return inventoryRepository.findByStoreAndDateRange(store.getId(), startDate, endDate).stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    // 구매자용: 매장의 날짜별 상품 조회
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAvailableInventory(Long storeId, LocalDate date) {
        return inventoryRepository.findAvailableByStoreAndDate(storeId, date).stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocalDate> getAvailableDates(Long storeId) {
        return inventoryRepository.findAvailableDatesByStore(storeId, LocalDate.now());
    }
}
