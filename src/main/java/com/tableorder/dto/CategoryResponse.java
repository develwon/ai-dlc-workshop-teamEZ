package com.tableorder.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Integer displayOrder;
}
