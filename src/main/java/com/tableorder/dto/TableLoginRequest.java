package com.tableorder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TableLoginRequest {

    @NotBlank
    private String storeCode;

    @NotNull
    @Min(1)
    private Integer tableNumber;

    @NotBlank
    private String password;
}
