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

@Service
@RequiredArgsConstructor
@Slf4j
public class TableSessionService {

    private final TableSessionRepository tableSessionRepository;
    private final OrderRepository orderRepository;
    private final OrderHistoryService orderHistoryService;

    @Transactional(readOnly = true)
    public Optional<TableSession> getActiveSession(Long tableId) {
        return tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId);
    }

    @Transactional
    public TableSession getOrCreateSession(Long tableId) {
        return tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId)
                .orElseGet(() -> {
                    TableSession session = TableSession.builder()
                            .tableId(tableId)
                            .build();
                    log.info("New session created for table {}", tableId);
                    return tableSessionRepository.save(session);
                });
    }

    @Transactional
    public void completeSession(Long tableId, Long storeId) {
        TableSession session = tableSessionRepository.findByTableIdAndEndTimeIsNull(tableId)
                .orElseThrow(() -> new IllegalStateException("활성 세션이 없습니다."));

        long orderCount = orderRepository.countBySessionIdAndDeletedFalse(session.getId());
        if (orderCount == 0) {
            throw new IllegalStateException("주문 내역이 없습니다.");
        }

        boolean hasIncompleteOrders = orderRepository
                .existsBySessionIdAndDeletedFalseAndStatusNot(session.getId(), OrderStatus.COMPLETED);
        if (hasIncompleteOrders) {
            throw new IllegalStateException("미완료 주문이 있습니다. 모든 주문을 완료 처리한 후 이용 완료해주세요.");
        }

        orderHistoryService.archiveSessionOrders(tableId, session.getId(), storeId);

        orderRepository.softDeleteBySessionId(session.getId());

        session.endSession();
        tableSessionRepository.save(session);

        log.info("Session completed [tableId={}, sessionId={}, storeId={}, archivedOrders={}]",
                tableId, session.getId(), storeId, orderCount);
    }
}
