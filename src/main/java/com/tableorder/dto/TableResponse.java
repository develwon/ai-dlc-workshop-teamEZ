package com.tableorder.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TableResponse {
    private Long id;
    private Long storeId;
    private Integer tableNumber;
    private LocalDateTime createdAt;
}
