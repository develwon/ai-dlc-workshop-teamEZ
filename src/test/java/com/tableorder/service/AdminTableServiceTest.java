package com.tableorder.service;

import com.tableorder.dto.CreateTableRequest;
import com.tableorder.dto.TableResponse;
import com.tableorder.dto.TableSummaryResponse;
import com.tableorder.entity.StoreTable;
import com.tableorder.entity.TableSession;
import com.tableorder.repository.StoreTableRepository;
import com.tableorder.repository.TableSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminTableServiceTest {

    @Mock
    private StoreTableRepository storeTableRepository;
    @Mock
    private TableSessionRepository tableSessionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminTableService adminTableService;

    @Test
    @DisplayName("테이블 설정 - 신규 생성")
    void createOrUpdateTable_newTable() {
        // given
        Long storeId = 100L;
        CreateTableRequest request = new CreateTableRequest(5, "1234");

        given(passwordEncoder.encode(anyString())).willReturn("$2a$10$encoded");
        given(storeTableRepository.findByStoreIdAndTableNumber(storeId, 5))
                .willReturn(Optional.empty());

        StoreTable saved = StoreTable.builder()
                .storeId(storeId).tableNumber(5).passwordHash("$2a$10$encoded")
                .build();
        given(storeTableRepository.save(any(StoreTable.class))).willReturn(saved);

        // when
        TableResponse response = adminTableService.createOrUpdateTable(storeId, request);

        // then
        assertThat(response.getTableNumber()).isEqualTo(5);
        verify(storeTableRepository).save(any(StoreTable.class));
    }

    @Test
    @DisplayName("테이블 설정 - 기존 테이블 비밀번호 업데이트")
    void createOrUpdateTable_updateExisting() {
        // given
        Long storeId = 100L;
        CreateTableRequest request = new CreateTableRequest(5, "newpass");

        StoreTable existing = StoreTable.builder()
                .storeId(storeId).tableNumber(5).passwordHash("$2a$10$old")
                .build();

        given(passwordEncoder.encode(anyString())).willReturn("$2a$10$new");
        given(storeTableRepository.findByStoreIdAndTableNumber(storeId, 5))
                .willReturn(Optional.of(existing));
        given(storeTableRepository.save(any(StoreTable.class))).willReturn(existing);

        // when
        TableResponse response = adminTableService.createOrUpdateTable(storeId, request);

        // then
        assertThat(response).isNotNull();
        verify(storeTableRepository).save(existing);
    }

    @Test
    @DisplayName("테이블 목록 조회 - 활성 세션 여부 포함")
    void getTables_withActiveSessionInfo() {
        // given
        Long storeId = 100L;
        StoreTable table1 = StoreTable.builder().storeId(storeId).tableNumber(1).passwordHash("hash").build();
        StoreTable table2 = StoreTable.builder().storeId(storeId).tableNumber(2).passwordHash("hash").build();

        given(storeTableRepository.findAllByStoreIdOrderByTableNumberAsc(storeId))
                .willReturn(List.of(table1, table2));
        given(tableSessionRepository.findByTableIdAndEndTimeIsNull(table1.getId()))
                .willReturn(Optional.of(TableSession.builder().tableId(table1.getId()).build()));
        given(tableSessionRepository.findByTableIdAndEndTimeIsNull(table2.getId()))
                .willReturn(Optional.empty());

        // when
        List<TableSummaryResponse> result = adminTableService.getTables(storeId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).isHasActiveSession()).isTrue();
        assertThat(result.get(1).isHasActiveSession()).isFalse();
    }
}
