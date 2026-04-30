package com.tableorder.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class TableSummaryResponse {
    private Long tableId;
    private Integer tableNumber;
    private Integer totalOrderAmount;
    private Integer activeOrderCount;
    private List<AdminOrderResponse> latestOrders;
}
