package com.tableorder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTableRequest {

    @NotNull
    @Min(1)
    private Integer tableNumber;

    @NotBlank
    @Size(min = 4)
    private String password;
}
