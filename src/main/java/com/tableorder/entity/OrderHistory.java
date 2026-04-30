package com.tableorder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "order_number", length = 20, nullable = false)
    private String orderNumber;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "order_items", nullable = false, columnDefinition = "TEXT")
    private String orderItems;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public OrderHistory(Long storeId, Long tableId, Long sessionId,
                        String orderNumber, Integer totalAmount, String orderItems,
                        LocalDateTime orderedAt, LocalDateTime completedAt) {
        this.storeId = storeId;
        this.tableId = tableId;
        this.sessionId = sessionId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
        this.orderedAt = orderedAt;
        this.completedAt = completedAt;
        this.createdAt = LocalDateTime.now();
    }
}
