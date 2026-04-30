package com.tableorder.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class MenuResponse {
    private Long id;
    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private Long categoryId;
    private Integer displayOrder;
}
