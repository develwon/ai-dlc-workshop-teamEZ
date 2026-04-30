package com.tableorder.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long id;
    private String orderNumber;
    private Integer totalAmount;
    private String orderItems;
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;
}
