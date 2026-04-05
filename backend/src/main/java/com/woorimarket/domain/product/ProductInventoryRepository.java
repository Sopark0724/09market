package com.woorimarket.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long> {

    List<ProductInventory> findByProductIdAndAvailableDate(Long productId, LocalDate date);

    @Query("SELECT pi FROM ProductInventory pi JOIN pi.product p " +
           "WHERE p.store.id = :storeId AND pi.availableDate = :date AND pi.status = 'AVAILABLE'")
    List<ProductInventory> findAvailableByStoreAndDate(
            @Param("storeId") Long storeId,
            @Param("date") LocalDate date);

    @Query("SELECT pi FROM ProductInventory pi JOIN pi.product p " +
           "WHERE p.store.id = :storeId AND pi.availableDate BETWEEN :startDate AND :endDate")
    List<ProductInventory> findByStoreAndDateRange(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT pi.availableDate FROM ProductInventory pi JOIN pi.product p " +
           "WHERE p.store.id = :storeId AND pi.status = 'AVAILABLE' AND pi.availableDate >= :fromDate " +
           "ORDER BY pi.availableDate")
    List<LocalDate> findAvailableDatesByStore(
            @Param("storeId") Long storeId,
            @Param("fromDate") LocalDate fromDate);
}
