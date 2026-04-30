package com.tableorder.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long storeId;
    private Long tableId;
    private Integer tableNumber;
    private Long sessionId;
    private Integer totalAmount;
    private String status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
