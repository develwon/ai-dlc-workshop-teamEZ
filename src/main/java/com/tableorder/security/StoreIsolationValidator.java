package com.tableorder.security;

import com.tableorder.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class StoreIsolationValidator {

    public void validateStoreAccess(Long jwtStoreId, Long requestStoreId) {
        if (!jwtStoreId.equals(requestStoreId)) {
            throw new ForbiddenException("매장 접근 권한이 없습니다.");
        }
    }
}
