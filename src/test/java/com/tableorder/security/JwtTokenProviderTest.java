package com.tableorder.security;

import com.tableorder.enums.AdminRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Base64 encoded 256-bit key
        String base64Secret = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZzEyMzQ1Njc4OTA=";
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", base64Secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", 57600000L);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("테이블 토큰 생성 및 검증")
    void testGenerateAndValidateTableToken() {
        Long tableId = 5L;
        Long storeId = 1L;

        String token = jwtTokenProvider.generateTableToken(tableId, storeId);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getSubjectFromToken(token)).isEqualTo(tableId.toString());
        assertThat(jwtTokenProvider.getStoreIdFromToken(token)).isEqualTo(storeId);
        assertThat(jwtTokenProvider.getTypeFromToken(token)).isEqualTo("TABLE");
    }

    @Test
    @DisplayName("관리자 토큰 생성 및 검증")
    void testGenerateAndValidateAdminToken() {
        Long adminId = 10L;
        Long storeId = 2L;
        AdminRole role = AdminRole.OWNER;

        String token = jwtTokenProvider.generateAdminToken(adminId, storeId, role);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getSubjectFromToken(token)).isEqualTo(adminId.toString());
        assertThat(jwtTokenProvider.getStoreIdFromToken(token)).isEqualTo(storeId);
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(AdminRole.OWNER);
        assertThat(jwtTokenProvider.getTypeFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패")
    void testInvalidTokenReturnsFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void testExpiredTokenReturnsFalse() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        String base64Secret = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZzEyMzQ1Njc4OTA=";
        ReflectionTestUtils.setField(expiredProvider, "secret", base64Secret);
        ReflectionTestUtils.setField(expiredProvider, "expiration", -1000L);
        expiredProvider.init();

        String token = expiredProvider.generateTableToken(1L, 1L);

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }
}
