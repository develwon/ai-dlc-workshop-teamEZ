package com.tableorder.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class OrderEventData {
    private String eventType;
    private Long orderId;
    private String orderNumber;
    private Long tableId;
    private Integer tableNumber;
    private Integer totalAmount;
    private String status;
    private LocalDateTime timestamp;
}
