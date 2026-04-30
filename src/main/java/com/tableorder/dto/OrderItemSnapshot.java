package com.tableorder.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemSnapshot {
    private String menuName;
    private int quantity;
    private int unitPrice;
}
