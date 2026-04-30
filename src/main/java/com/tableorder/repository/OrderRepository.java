package com.tableorder.repository;

import com.tableorder.entity.Order;
import com.tableorder.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findBySessionIdOrderByCreatedAtDesc(Long sessionId);

    List<Order> findByStoreIdAndStatusInOrderByCreatedAtDesc(Long storeId, List<OrderStatus> statuses);

    List<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<Order> findByStoreIdAndTableIdOrderByCreatedAtDesc(Long storeId, Long tableId);

    List<Order> findBySessionId(Long sessionId);

    Optional<Order> findByIdAndStoreId(Long id, Long storeId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.sessionId = :sessionId")
    Integer sumTotalAmountBySessionId(@Param("sessionId") Long sessionId);

    boolean existsBySessionId(Long sessionId);
}
