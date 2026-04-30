package com.tableorder.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemResponse {
    private Long menuId;
    private String menuName;
    private Integer quantity;
    private Integer unitPrice;
    private Integer subtotal;
}
