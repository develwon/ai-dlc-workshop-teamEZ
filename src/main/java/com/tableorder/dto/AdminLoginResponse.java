package com.tableorder.dto;

import com.tableorder.enums.AdminRole;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class AdminLoginResponse {
    private String token;
    private AdminRole role;
    private Long storeId;
    private String storeName;
}
