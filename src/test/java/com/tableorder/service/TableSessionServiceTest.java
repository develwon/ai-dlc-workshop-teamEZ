package com.tableorder.service;

import com.tableorder.entity.TableSession;
import com.tableorder.enums.OrderStatus;
import com.tableorder.exception.NotFoundException;
import com.tableorder.repository.OrderRepository;
import com.tableorder.repository.TableSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TableSessionServiceTest {

    @InjectMocks
    private TableSessionService tableSessionService;

    @Mock
    private TableSessionRepository tableSessionRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderHistoryService orderHistoryService;

    private static final Long TABLE_ID = 1L;
    private static final Long STORE_ID = 100L;

    @Nested
    @DisplayName("세션 조회 또는 생성")
    class GetOrCreateSession {

        @Test
        @DisplayName("활성 세션이 있으면 기존 세션을 반환한다")
        void getOrCreateSession_existingSession() {
            TableSession existingSession = TableSession.builder().tableId(TABLE_ID).build();
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.of(existingSession));

            TableSession result = tableSessionService.getOrCreateSession(TABLE_ID);

            assertThat(result).isEqualTo(existingSession);
            then(tableSessionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("활성 세션이 없으면 새 세션을 생성한다")
        void getOrCreateSession_newSession() {
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.empty());
            TableSession newSession = TableSession.builder().tableId(TABLE_ID).build();
            given(tableSessionRepository.save(any(TableSession.class))).willReturn(newSession);

            TableSession result = tableSessionService.getOrCreateSession(TABLE_ID);

            assertThat(result).isNotNull();
            assertThat(result.getTableId()).isEqualTo(TABLE_ID);
            then(tableSessionRepository).should().save(any(TableSession.class));
        }
    }

    @Nested
    @DisplayName("활성 세션 조회")
    class GetActiveSession {

        @Test
        @DisplayName("활성 세션이 있으면 반환한다")
        void getActiveSession_found() {
            TableSession session = TableSession.builder().tableId(TABLE_ID).build();
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.of(session));

            Optional<TableSession> result = tableSessionService.getActiveSession(TABLE_ID);

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("활성 세션이 없으면 빈 Optional을 반환한다")
        void getActiveSession_notFound() {
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.empty());

            Optional<TableSession> result = tableSessionService.getActiveSession(TABLE_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("단순 세션 종료 (Unit 4)")
    class CompleteSessionById {

        @Test
        @DisplayName("활성 세션을 정상적으로 종료한다")
        void completeSession_success() {
            TableSession session = TableSession.builder().tableId(TABLE_ID).build();
            given(tableSessionRepository.findById(1L)).willReturn(Optional.of(session));

            tableSessionService.completeSession(1L);

            assertThat(session.isActive()).isFalse();
            then(tableSessionRepository).should().save(session);
        }

        @Test
        @DisplayName("존재하지 않는 세션 종료 시 예외 발생")
        void completeSession_notFound() {
            given(tableSessionRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> tableSessionService.completeSession(999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("이용 완료 (Unit 5)")
    class CompleteSessionWithValidation {

        @Test
        @DisplayName("이용 완료 - 성공")
        void completeSession_success() {
            TableSession session = TableSession.builder().tableId(TABLE_ID).build();
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.of(session));
            given(orderRepository.countBySessionIdAndDeletedFalse(session.getId())).willReturn(3L);
            given(orderRepository.existsBySessionIdAndDeletedFalseAndStatusNot(session.getId(), OrderStatus.COMPLETED))
                    .willReturn(false);
            given(orderRepository.softDeleteBySessionId(session.getId())).willReturn(3);

            tableSessionService.completeSession(TABLE_ID, STORE_ID);

            then(orderHistoryService).should().archiveSessionOrders(TABLE_ID, session.getId(), STORE_ID);
            then(orderRepository).should().softDeleteBySessionId(session.getId());
            then(tableSessionRepository).should().save(session);
            assertThat(session.getEndTime()).isNotNull();
        }

        @Test
        @DisplayName("이용 완료 실패 - 활성 세션 없음")
        void completeSession_noActiveSession() {
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> tableSessionService.completeSession(TABLE_ID, STORE_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("활성 세션이 없습니다.");
        }

        @Test
        @DisplayName("이용 완료 실패 - 주문 없음")
        void completeSession_noOrders() {
            TableSession session = TableSession.builder().tableId(TABLE_ID).build();
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.of(session));
            given(orderRepository.countBySessionIdAndDeletedFalse(session.getId())).willReturn(0L);

            assertThatThrownBy(() -> tableSessionService.completeSession(TABLE_ID, STORE_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("주문 내역이 없습니다.");
        }

        @Test
        @DisplayName("이용 완료 실패 - 미완료 주문 존재")
        void completeSession_incompleteOrders() {
            TableSession session = TableSession.builder().tableId(TABLE_ID).build();
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.of(session));
            given(orderRepository.countBySessionIdAndDeletedFalse(session.getId())).willReturn(2L);
            given(orderRepository.existsBySessionIdAndDeletedFalseAndStatusNot(session.getId(), OrderStatus.COMPLETED))
                    .willReturn(true);

            assertThatThrownBy(() -> tableSessionService.completeSession(TABLE_ID, STORE_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("미완료 주문이 있습니다");
        }
    }
}
