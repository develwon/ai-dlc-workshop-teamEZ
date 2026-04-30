package com.tableorder.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminOrderResponse {
    private Long id;
    private String orderNumber;
    private Long tableId;
    private Integer tableNumber;
    private Integer totalAmount;
    private String status;
    private Integer itemCount;
    private LocalDateTime createdAt;
}
