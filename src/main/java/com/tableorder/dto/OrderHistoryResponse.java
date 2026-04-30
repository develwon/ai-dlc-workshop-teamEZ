package com.tableorder.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long id;
    private String orderNumber;
    private Integer totalAmount;
    private List<OrderItemSnapshot> orderItems;
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;
}
