package com.tableorder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "table_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TableSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TableSession(Long tableId) {
        this.tableId = tableId;
        this.startTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public void endSession() {
        this.endTime = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.endTime == null;
    }
}
