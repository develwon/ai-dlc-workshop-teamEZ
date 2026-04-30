package com.tableorder.repository;

import com.tableorder.entity.OrderHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrderHistoryRepositoryTest {

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    private Long tableId = 1L;

    @BeforeEach
    void setUp() {
        orderHistoryRepository.deleteAll();

        OrderHistory history1 = OrderHistory.builder()
                .storeId(100L).tableId(tableId).sessionId(10L)
                .orderNumber("ORD-001").totalAmount(15000)
                .orderItems("[{\"menuName\":\"김치찌개\",\"quantity\":1,\"unitPrice\":9000}]")
                .orderedAt(LocalDateTime.of(2026, 4, 29, 12, 0))
                .completedAt(LocalDateTime.of(2026, 4, 29, 14, 0))
                .build();

        OrderHistory history2 = OrderHistory.builder()
                .storeId(100L).tableId(tableId).sessionId(11L)
                .orderNumber("ORD-002").totalAmount(20000)
                .orderItems("[{\"menuName\":\"된장찌개\",\"quantity\":2,\"unitPrice\":8000}]")
                .orderedAt(LocalDateTime.of(2026, 4, 30, 12, 0))
                .completedAt(LocalDateTime.of(2026, 4, 30, 14, 0))
                .build();

        orderHistoryRepository.saveAll(List.of(history1, history2));
    }

    @Test
    @DisplayName("테이블별 전체 이력 조회 - completedAt 내림차순")
    void findByTableIdOrderByCompletedAtDesc() {
        List<OrderHistory> result = orderHistoryRepository.findByTableIdOrderByCompletedAtDesc(tableId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-002");
        assertThat(result.get(1).getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    @DisplayName("orderedAt 날짜 범위 필터 조회")
    void findByTableIdAndOrderedAtBetween() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 30, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59, 59);

        List<OrderHistory> result = orderHistoryRepository
                .findByTableIdAndOrderedAtBetweenOrderByOrderedAtDesc(tableId, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-002");
    }

    @Test
    @DisplayName("completedAt 날짜 범위 필터 조회")
    void findByTableIdAndCompletedAtBetween() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 29, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 29, 23, 59, 59);

        List<OrderHistory> result = orderHistoryRepository
                .findByTableIdAndCompletedAtBetweenOrderByCompletedAtDesc(tableId, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    @DisplayName("존재하지 않는 테이블 ID로 조회 시 빈 목록")
    void findByTableId_notFound() {
        List<OrderHistory> result = orderHistoryRepository.findByTableIdOrderByCompletedAtDesc(999L);

        assertThat(result).isEmpty();
    }
}
