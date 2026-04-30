package com.tableorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotBlank
    private String status;
}
