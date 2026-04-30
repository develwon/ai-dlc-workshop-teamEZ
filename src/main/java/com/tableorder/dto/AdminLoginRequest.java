package com.tableorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequest {

    @NotBlank
    private String storeCode;

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
