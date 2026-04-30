package com.tableorder.service;

import com.tableorder.dto.AdminLoginRequest;
import com.tableorder.dto.AdminLoginResponse;
import com.tableorder.entity.Admin;
import com.tableorder.entity.Store;
import com.tableorder.exception.AccountLockedException;
import com.tableorder.exception.UnauthorizedException;
import com.tableorder.repository.AdminRepository;
import com.tableorder.repository.StoreRepository;
import com.tableorder.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final StoreRepository storeRepository;
    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.security.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    @Transactional
    public AdminLoginResponse authenticate(AdminLoginRequest request) {
        Store store = storeRepository.findByStoreCode(request.getStoreCode())
                .orElseThrow(() -> new UnauthorizedException("인증에 실패했습니다."));

        Admin admin = adminRepository.findByStoreIdAndUsername(store.getId(), request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("인증에 실패했습니다."));

        if (admin.isLocked()) {
            throw new AccountLockedException(
                    "계정이 잠겨 있습니다.",
                    admin.getLockedUntil());
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            admin.incrementLoginAttempts();

            if (admin.getLoginAttempts() >= maxLoginAttempts) {
                admin.lockAccount(lockDurationMinutes);
                log.warn("관리자 계정 잠금. 매장: {}, 사용자: {}", store.getStoreCode(), admin.getUsername());
                throw new AccountLockedException(
                        "로그인 시도 횟수를 초과하여 계정이 잠겼습니다.",
                        admin.getLockedUntil());
            }

            throw new UnauthorizedException("인증에 실패했습니다.");
        }

        admin.resetLoginAttempts();

        String token = jwtTokenProvider.generateAdminToken(
                admin.getId(), store.getId(), admin.getRole());

        log.info("관리자 로그인 성공. 매장: {}, 사용자: {}, 역할: {}",
                store.getStoreCode(), admin.getUsername(), admin.getRole());

        return AdminLoginResponse.builder()
                .token(token)
                .role(admin.getRole())
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .build();
    }
}
