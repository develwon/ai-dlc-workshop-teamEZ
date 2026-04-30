package com.tableorder.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ValidationErrorResponse {
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private List<FieldError> fieldErrors;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}
