package com.tableorder.service;

import com.tableorder.dto.AdminLoginRequest;
import com.tableorder.dto.AdminLoginResponse;
import com.tableorder.entity.Admin;
import com.tableorder.entity.Store;
import com.tableorder.enums.AdminRole;
import com.tableorder.exception.AccountLockedException;
import com.tableorder.exception.UnauthorizedException;
import com.tableorder.repository.AdminRepository;
import com.tableorder.repository.StoreRepository;
import com.tableorder.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminAuthService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(adminAuthService, "lockDurationMinutes", 30);
    }

    @Test
    @DisplayName("관리자 인증 성공")
    void testAuthenticateSuccess() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("STORE001", "admin", "password123");

        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        Admin admin = Admin.builder()
                .storeId(1L)
                .username("admin")
                .passwordHash("encodedPassword")
                .role(AdminRole.OWNER)
                .build();
        ReflectionTestUtils.setField(admin, "id", 5L);

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(adminRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAdminToken(5L, 1L, AdminRole.OWNER)).thenReturn("admin-jwt-token");

        // when
        AdminLoginResponse response = adminAuthService.authenticate(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("admin-jwt-token");
        assertThat(response.getRole()).isEqualTo(AdminRole.OWNER);
        assertThat(response.getStoreId()).isEqualTo(1L);
        assertThat(response.getStoreName()).isEqualTo("테스트 매장");
        assertThat(admin.getLoginAttempts()).isEqualTo(0);
    }

    @Test
    @DisplayName("관리자 인증 실패 - 잘못된 비밀번호")
    void testAuthenticateFailsWithWrongPassword() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("STORE001", "admin", "wrongPassword");

        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        Admin admin = Admin.builder()
                .storeId(1L)
                .username("admin")
                .passwordHash("encodedPassword")
                .role(AdminRole.OWNER)
                .build();
        ReflectionTestUtils.setField(admin, "id", 5L);

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(adminRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> adminAuthService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증에 실패했습니다.");

        assertThat(admin.getLoginAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자 인증 실패 - 계정 잠금 상태")
    void testAuthenticateFailsWhenAccountLocked() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("STORE001", "admin", "password123");

        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        Admin admin = Admin.builder()
                .storeId(1L)
                .username("admin")
                .passwordHash("encodedPassword")
                .role(AdminRole.OWNER)
                .build();
        ReflectionTestUtils.setField(admin, "id", 5L);
        // 계정을 잠금 상태로 설정
        ReflectionTestUtils.setField(admin, "lockedUntil", LocalDateTime.now().plusMinutes(30));

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(adminRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(admin));

        // when & then
        assertThatThrownBy(() -> adminAuthService.authenticate(request))
                .isInstanceOf(AccountLockedException.class)
                .hasMessage("계정이 잠겨 있습니다.");
    }

    @Test
    @DisplayName("관리자 인증 실패 - 최대 시도 횟수 초과로 계정 잠금")
    void testAuthenticateLocksAccountAfterMaxAttempts() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("STORE001", "admin", "wrongPassword");

        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        Admin admin = Admin.builder()
                .storeId(1L)
                .username("admin")
                .passwordHash("encodedPassword")
                .role(AdminRole.OWNER)
                .build();
        ReflectionTestUtils.setField(admin, "id", 5L);

        // 이미 4번 실패한 상태로 설정
        admin.incrementLoginAttempts();
        admin.incrementLoginAttempts();
        admin.incrementLoginAttempts();
        admin.incrementLoginAttempts();

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(adminRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then (5번째 시도로 계정 잠금)
        assertThatThrownBy(() -> adminAuthService.authenticate(request))
                .isInstanceOf(AccountLockedException.class)
                .hasMessage("로그인 시도 횟수를 초과하여 계정이 잠겼습니다.");

        assertThat(admin.getLoginAttempts()).isEqualTo(5);
        assertThat(admin.getLockedUntil()).isNotNull();
    }

    @Test
    @DisplayName("관리자 인증 실패 - 잘못된 매장 코드")
    void testAuthenticateFailsWithInvalidStoreCode() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("INVALID", "admin", "password123");

        when(storeRepository.findByStoreCode("INVALID")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminAuthService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증에 실패했습니다.");
    }
}
