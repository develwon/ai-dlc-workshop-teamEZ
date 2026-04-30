package com.tableorder.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Min(0)
    @Max(10000000)
    private Integer price;

    @Size(max = 500)
    private String description;

    @NotNull
    private Long categoryId;

    private String imageUrl;
}
