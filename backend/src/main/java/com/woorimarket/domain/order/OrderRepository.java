package com.woorimarket.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Order> findByStoreIdAndOrderDate(Long storeId, LocalDate orderDate);

    List<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate")
    List<Order> findByStoreAndDateRange(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId AND o.orderDate = :date AND o.status != 'CANCELLED'")
    Long countByStoreAndDate(@Param("storeId") Long storeId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate AND o.status != 'CANCELLED'")
    java.math.BigDecimal sumTotalByStoreAndDateRange(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
