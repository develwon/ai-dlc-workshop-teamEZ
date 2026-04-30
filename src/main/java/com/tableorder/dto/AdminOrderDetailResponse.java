package com.tableorder.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminOrderDetailResponse {
    private Long id;
    private String orderNumber;
    private Long tableId;
    private Integer tableNumber;
    private Integer totalAmount;
    private String status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
