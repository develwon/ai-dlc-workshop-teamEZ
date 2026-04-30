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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableAuthServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreTableRepository storeTableRepository;

    @Mock
    private TableSessionRepository tableSessionRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TableAuthService tableAuthService;

    @Test
    @DisplayName("테이블 인증 성공 - 활성 세션 없음")
    void testAuthenticateSuccess() {
        // given
        TableLoginRequest request = new TableLoginRequest("STORE001", 1, "password123");

        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        StoreTable table = StoreTable.builder()
                .storeId(1L)
                .tableNumber(1)
                .passwordHash("encodedPassword")
                .build();
        ReflectionTestUtils.setField(table, "id", 10L);

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(storeTableRepository.findByStoreIdAndTableNumber(1L, 1)).thenReturn(Optional.of(table));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(tableSessionRepository.findByTableIdAndEndTimeIsNull(10L)).thenReturn(Optional.empty());
        when(jwtTokenProvider.generateTableToken(10L, 1L)).thenReturn("jwt-token");

        // when
        TableLoginResponse response = tableAuthService.authenticate(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTableId()).isEqualTo(10L);
        assertThat(response.getStoreId()).isEqualTo(1L);
        assertThat(response.getSessionId()).isNull();
    }

    @Test
    @DisplayName("테이블 인증 성공 - 활성 세션 있음")
    void testAuthenticateSuccessWithActiveSession() {
        // given
        TableLoginRequest request = new TableLoginRequest("STORE001", 1, "password123");

        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        StoreTable table = StoreTable.builder()
                .storeId(1L)
                .tableNumber(1)
                .passwordHash("encodedPassword")
                .build();
        ReflectionTestUtils.setField(table, "id", 10L);

        TableSession session = TableSession.builder()
                .tableId(10L)
                .build();
        ReflectionTestUtils.setField(session, "id", 100L);

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(storeTableRepository.findByStoreIdAndTableNumber(1L, 1)).thenReturn(Optional.of(table));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(tableSessionRepository.findByTableIdAndEndTimeIsNull(10L)).thenReturn(Optional.of(session));
        when(jwtTokenProvider.generateTableToken(10L, 1L)).thenReturn("jwt-token");

        // when
        TableLoginResponse response = tableAuthService.authenticate(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTableId()).isEqualTo(10L);
        assertThat(response.getStoreId()).isEqualTo(1L);
        assertThat(response.getSessionId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("테이블 인증 실패 - 잘못된 매장 코드")
    void testAuthenticateFailsWithInvalidStoreCode() {
        // given
        TableLoginRequest request = new TableLoginRequest("INVALID", 1, "password123");

        when(storeRepository.findByStoreCode("INVALID")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tableAuthService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증에 실패했습니다.");
    }

    @Test
    @DisplayName("테이블 인증 실패 - 잘못된 테이블 번호")
    void testAuthenticateFailsWithInvalidTableNumber() {
        // given
        TableLoginRequest request = new TableLoginRequest("STORE001", 99, "password123");

        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(storeTableRepository.findByStoreIdAndTableNumber(1L, 99)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tableAuthService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증에 실패했습니다.");
    }

    @Test
    @DisplayName("테이블 인증 실패 - 잘못된 비밀번호")
    void testAuthenticateFailsWithWrongPassword() {
        // given
        TableLoginRequest request = new TableLoginRequest("STORE001", 1, "wrongPassword");

        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        StoreTable table = StoreTable.builder()
                .storeId(1L)
                .tableNumber(1)
                .passwordHash("encodedPassword")
                .build();
        ReflectionTestUtils.setField(table, "id", 10L);

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(storeTableRepository.findByStoreIdAndTableNumber(1L, 1)).thenReturn(Optional.of(table));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> tableAuthService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증에 실패했습니다.");
    }
}
