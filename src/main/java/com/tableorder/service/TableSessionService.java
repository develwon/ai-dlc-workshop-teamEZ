package com.tableorder.service;

import com.tableorder.entity.TableSession;
import com.tableorder.enums.OrderStatus;
import com.tableorder.exception.NotFoundException;
import com.tableorder.repository.OrderRepository;
import com.tableorder.repository.TableSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableSessionService {

    private final TableSessionRepository tableSessionRepository;
    private final OrderRepository orderRepository;
    private final OrderHistoryService orderHistoryService;

    @Transactional
    public TableSession getOrCreateSession(Long tableId) {
        Optional<TableSession> activeSession = tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId);

        if (activeSession.isPresent()) {
            log.debug("테이블 {} 기존 활성 세션 사용: {}", tableId, activeSession.get().getId());
            return activeSession.get();
        }

        TableSession newSession = TableSession.builder()
                .tableId(tableId)
                .build();
        TableSession saved = tableSessionRepository.save(newSession);
        log.info("테이블 {} 새 세션 생성: {}", tableId, saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<TableSession> getActiveSession(Long tableId) {
        return tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId);
    }

    @Transactional(readOnly = true)
    public TableSession getSessionById(Long sessionId) {
        return tableSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("세션을 찾을 수 없습니다. ID: " + sessionId));
    }

    /**
     * Unit 5: 테이블 이용 완료 (검증 + 아카이빙 + 세션 종료)
     */
    @Transactional
    public void completeSession(Long tableId, Long storeId) {
        TableSession session = tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId)
                .orElseThrow(() -> new IllegalStateException("활성 세션이 없습니다."));

        long orderCount = orderRepository.countBySessionIdAndDeletedFalse(session.getId());
        if (orderCount == 0) {
            throw new IllegalStateException("주문 내역이 없습니다.");
        }

        orderHistoryService.archiveSessionOrders(tableId, session.getId(), storeId);

        orderRepository.softDeleteBySessionId(session.getId());

        session.endSession();
        tableSessionRepository.save(session);

        log.info("Session completed [tableId={}, sessionId={}, storeId={}, archivedOrders={}]",
                tableId, session.getId(), storeId, orderCount);
    }

    /**
     * Unit 4: 단순 세션 종료 (sessionId 기반)
     */
    @Transactional
    public void completeSession(Long sessionId) {
        TableSession session = getSessionById(sessionId);
        if (!session.isActive()) {
            log.warn("이미 종료된 세션입니다. ID: {}", sessionId);
            return;
        }
        session.endSession();
        tableSessionRepository.save(session);
        log.info("세션 종료 완료. ID: {}", sessionId);
    }
}
