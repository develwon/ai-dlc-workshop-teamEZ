package com.tableorder.entity;

import com.tableorder.enums.AdminRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTest {

    @Test
    @DisplayName("로그인 시도 횟수 증가")
    void testIncrementLoginAttempts() {
        Admin admin = Admin.builder()
                .storeId(1L)
                .username("testuser")
                .passwordHash("hashedpw")
                .role(AdminRole.OWNER)
                .build();

        admin.incrementLoginAttempts();

        assertThat(admin.getLoginAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("로그인 시도 횟수 초기화")
    void testResetLoginAttempts() {
        Admin admin = Admin.builder()
                .storeId(1L)
                .username("testuser")
                .passwordHash("hashedpw")
                .role(AdminRole.OWNER)
                .build();

        admin.incrementLoginAttempts();
        admin.incrementLoginAttempts();
        admin.resetLoginAttempts();

        assertThat(admin.getLoginAttempts()).isEqualTo(0);
        assertThat(admin.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("계정 잠금 처리")
    void testLockAccount() {
        Admin admin = Admin.builder()
                .storeId(1L)
                .username("testuser")
                .passwordHash("hashedpw")
                .role(AdminRole.OWNER)
                .build();

        admin.lockAccount(30);

        assertThat(admin.isLocked()).isTrue();
    }

    @Test
    @DisplayName("잠금 시간이 과거인 경우 잠금 해제 상태")
    void testIsNotLockedWhenLockedUntilInPast() {
        Admin admin = Admin.builder()
                .storeId(1L)
                .username("testuser")
                .passwordHash("hashedpw")
                .role(AdminRole.OWNER)
                .build();

        ReflectionTestUtils.setField(admin, "lockedUntil", LocalDateTime.now().minusMinutes(10));

        assertThat(admin.isLocked()).isFalse();
    }
}
