package com.tableorder.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class TableLoginResponse {
    private String token;
    private Long tableId;
    private Long storeId;
    private Long sessionId;
}
