package com.tableorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull
    private Long storeId;

    @NotNull
    private Long tableId;

    private Long sessionId;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
