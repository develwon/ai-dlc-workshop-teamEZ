package com.tableorder.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuOrderRequest {
    private Long menuId;
    private Integer displayOrder;
}
