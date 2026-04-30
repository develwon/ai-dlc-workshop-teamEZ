package com.tableorder.service;

import com.tableorder.entity.TableSession;
import com.tableorder.exception.NotFoundException;
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

    private static final Long TABLE_ID = 1L;

    @Nested
    @DisplayName("세션 조회 또는 생성")
    class GetOrCreateSession {

        @Test
        @DisplayName("활성 세션이 있으면 기존 세션을 반환한다")
        void getOrCreateSession_existingSession() {
            // given
            TableSession existingSession = TableSession.builder()
                    .tableId(TABLE_ID)
                    .build();

            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.of(existingSession));

            // when
            TableSession result = tableSessionService.getOrCreateSession(TABLE_ID);

            // then
            assertThat(result).isEqualTo(existingSession);
            then(tableSessionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("활성 세션이 없으면 새 세션을 생성한다")
        void getOrCreateSession_newSession() {
            // given
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.empty());

            TableSession newSession = TableSession.builder()
                    .tableId(TABLE_ID)
                    .build();
            given(tableSessionRepository.save(any(TableSession.class)))
                    .willReturn(newSession);

            // when
            TableSession result = tableSessionService.getOrCreateSession(TABLE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTableId()).isEqualTo(TABLE_ID);
            then(tableSessionRepository).should().save(any(TableSession.class));
        }
    }

    @Nested
    @DisplayName("세션 종료")
    class CompleteSession {

        @Test
        @DisplayName("활성 세션을 정상적으로 종료한다")
        void completeSession_success() {
            // given
            TableSession session = TableSession.builder()
                    .tableId(TABLE_ID)
                    .build();

            given(tableSessionRepository.findById(1L))
                    .willReturn(Optional.of(session));

            // when
            tableSessionService.completeSession(1L);

            // then
            assertThat(session.isActive()).isFalse();
            then(tableSessionRepository).should().save(session);
        }

        @Test
        @DisplayName("존재하지 않는 세션 종료 시 예외 발생")
        void completeSession_notFound() {
            // given
            given(tableSessionRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> tableSessionService.completeSession(999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("활성 세션 조회")
    class GetActiveSession {

        @Test
        @DisplayName("활성 세션이 있으면 반환한다")
        void getActiveSession_found() {
            // given
            TableSession session = TableSession.builder()
                    .tableId(TABLE_ID)
                    .build();

            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.of(session));

            // when
            Optional<TableSession> result = tableSessionService.getActiveSession(TABLE_ID);

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("활성 세션이 없으면 빈 Optional을 반환한다")
        void getActiveSession_notFound() {
            // given
            given(tableSessionRepository.findByTableIdAndEndTimeIsNull(TABLE_ID))
                    .willReturn(Optional.empty());

            // when
            Optional<TableSession> result = tableSessionService.getActiveSession(TABLE_ID);

            // then
            assertThat(result).isEmpty();
        }
    }
}
