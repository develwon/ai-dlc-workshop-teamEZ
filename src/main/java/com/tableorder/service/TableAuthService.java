package com.tableorder.service;

import com.tableorder.dto.TableLoginRequest;
import com.tableorder.dto.TableLoginResponse;
import com.tableorder.entity.Store;
import com.tableorder.entity.StoreTable;
import com.tableorder.entity.TableSession;
import com.tableorder.exception.UnauthorizedException;
import com.tableorder.repository.StoreRepository;
import com.tableorder.repository.StoreTableRepository;
import com.tableorder.repository.TableSessionRepository;
import com.tableorder.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableAuthService {

    private final StoreRepository storeRepository;
    private final StoreTableRepository storeTableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public TableLoginResponse authenticate(TableLoginRequest request) {
        Store store = storeRepository.findByStoreCode(request.getStoreCode())
                .orElseThrow(() -> new UnauthorizedException("인증에 실패했습니다."));

        StoreTable table = storeTableRepository
                .findByStoreIdAndTableNumber(store.getId(), request.getTableNumber())
                .orElseThrow(() -> new UnauthorizedException("인증에 실패했습니다."));

        if (!passwordEncoder.matches(request.getPassword(), table.getPasswordHash())) {
            throw new UnauthorizedException("인증에 실패했습니다.");
        }

        Long sessionId = tableSessionRepository
                .findByTableIdAndEndTimeIsNull(table.getId())
                .map(TableSession::getId)
                .orElse(null);

        String token = jwtTokenProvider.generateTableToken(table.getId(), store.getId());

        log.info("테이블 로그인 성공. 매장: {}, 테이블: {}", store.getStoreCode(), table.getTableNumber());

        return TableLoginResponse.builder()
                .token(token)
                .tableId(table.getId())
                .storeId(store.getId())
                .sessionId(sessionId)
                .build();
    }
}
