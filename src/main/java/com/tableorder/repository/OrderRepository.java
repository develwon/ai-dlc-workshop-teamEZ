package com.tableorder.repository;

import com.tableorder.entity.Order;
import com.tableorder.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findBySessionIdAndDeletedFalseOrderByCreatedAtDesc(Long sessionId);

    boolean existsBySessionIdAndDeletedFalseAndStatusNot(Long sessionId, OrderStatus status);

    long countBySessionIdAndDeletedFalse(Long sessionId);

    @Modifying
    @Query("UPDATE Order o SET o.deleted = true WHERE o.sessionId = :sessionId AND o.deleted = false")
    int softDeleteBySessionId(@Param("sessionId") Long sessionId);
}
