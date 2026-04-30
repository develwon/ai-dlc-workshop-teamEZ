package com.tableorder.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class TableSummaryResponse {
    private Long tableId;
    private Integer tableNumber;
    private boolean hasActiveSession;
}
