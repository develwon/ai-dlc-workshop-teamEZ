package com.tableorder.repository;

import com.tableorder.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    List<OrderHistory> findByTableIdOrderByCompletedAtDesc(Long tableId);

    List<OrderHistory> findByTableIdAndOrderedAtBetweenOrderByOrderedAtDesc(
            Long tableId, LocalDateTime start, LocalDateTime end);

    List<OrderHistory> findByTableIdAndCompletedAtBetweenOrderByCompletedAtDesc(
            Long tableId, LocalDateTime start, LocalDateTime end);
}
