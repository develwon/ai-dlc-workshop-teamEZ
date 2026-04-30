package com.tableorder.service;

import com.tableorder.entity.TableSession;
import com.tableorder.enums.OrderStatus;
import com.tableorder.repository.OrderRepository;
import com.tableorder.repository.TableSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableSessionServiceTest {

    @Mock
    private TableSessionRepository tableSessionRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderHistoryService orderHistoryService;

    @InjectMocks
    private TableSessionService tableSessionService;

    @Test
    @DisplayName("이용 완료 - 성공")
    void completeSession_success() {
        // given
        Long tableId = 1L;
        Long storeId = 100L;
        TableSession session = TableSession.builder().tableId(tableId).build();

        given(tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId))
                .willReturn(Optional.of(session));
        given(orderRepository.countBySessionIdAndDeletedFalse(session.getId()))
                .willReturn(3L);
        given(orderRepository.existsBySessionIdAndDeletedFalseAndStatusNot(session.getId(), OrderStatus.COMPLETED))
                .willReturn(false);
        given(orderRepository.softDeleteBySessionId(session.getId()))
                .willReturn(3);

        // when
        tableSessionService.completeSession(tableId, storeId);

        // then
        verify(orderHistoryService).archiveSessionOrders(tableId, session.getId(), storeId);
        verify(orderRepository).softDeleteBySessionId(session.getId());
        verify(tableSessionRepository).save(session);
        assertThat(session.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("이용 완료 실패 - 활성 세션 없음")
    void completeSession_noActiveSession() {
        // given
        Long tableId = 1L;
        Long storeId = 100L;

        given(tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tableSessionService.completeSession(tableId, storeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("활성 세션이 없습니다.");
    }

    @Test
    @DisplayName("이용 완료 실패 - 주문 없음")
    void completeSession_noOrders() {
        // given
        Long tableId = 1L;
        Long storeId = 100L;
        TableSession session = TableSession.builder().tableId(tableId).build();

        given(tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId))
                .willReturn(Optional.of(session));
        given(orderRepository.countBySessionIdAndDeletedFalse(session.getId()))
                .willReturn(0L);

        // when & then
        assertThatThrownBy(() -> tableSessionService.completeSession(tableId, storeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("주문 내역이 없습니다.");
    }

    @Test
    @DisplayName("이용 완료 실패 - 미완료 주문 존재")
    void completeSession_incompleteOrders() {
        // given
        Long tableId = 1L;
        Long storeId = 100L;
        TableSession session = TableSession.builder().tableId(tableId).build();

        given(tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId))
                .willReturn(Optional.of(session));
        given(orderRepository.countBySessionIdAndDeletedFalse(session.getId()))
                .willReturn(2L);
        given(orderRepository.existsBySessionIdAndDeletedFalseAndStatusNot(session.getId(), OrderStatus.COMPLETED))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> tableSessionService.completeSession(tableId, storeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("미완료 주문이 있습니다");
    }

    @Test
    @DisplayName("활성 세션 조회 또는 생성 - 기존 세션 반환")
    void getOrCreateSession_existingSession() {
        // given
        Long tableId = 1L;
        TableSession existing = TableSession.builder().tableId(tableId).build();

        given(tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId))
                .willReturn(Optional.of(existing));

        // when
        TableSession result = tableSessionService.getOrCreateSession(tableId);

        // then
        assertThat(result).isEqualTo(existing);
        verify(tableSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("활성 세션 조회 또는 생성 - 새 세션 생성")
    void getOrCreateSession_newSession() {
        // given
        Long tableId = 1L;
        TableSession newSession = TableSession.builder().tableId(tableId).build();

        given(tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId))
                .willReturn(Optional.empty());
        given(tableSessionRepository.save(any(TableSession.class)))
                .willReturn(newSession);

        // when
        TableSession result = tableSessionService.getOrCreateSession(tableId);

        // then
        assertThat(result).isNotNull();
        verify(tableSessionRepository).save(any(TableSession.class));
    }
}
