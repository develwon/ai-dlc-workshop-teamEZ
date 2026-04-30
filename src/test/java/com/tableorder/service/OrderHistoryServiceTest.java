package com.tableorder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.dto.OrderHistoryResponse;
import com.tableorder.entity.Order;
import com.tableorder.entity.OrderHistory;
import com.tableorder.entity.OrderItem;
import com.tableorder.repository.OrderHistoryRepository;
import com.tableorder.repository.OrderItemRepository;
import com.tableorder.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderHistoryServiceTest {

    @Mock
    private OrderHistoryRepository orderHistoryRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderHistoryService orderHistoryService;

    @Test
    @DisplayName("세션 주문 아카이빙 - 성공")
    void archiveSessionOrders_success() {
        // given
        Long tableId = 1L;
        Long sessionId = 10L;
        Long storeId = 100L;

        Order order = Order.builder()
                .storeId(storeId).tableId(tableId).sessionId(sessionId)
                .orderNumber("20260430-120000-ABCD").totalAmount(20000)
                .build();

        OrderItem item = OrderItem.builder()
                .menuId(1L).menuName("김치찌개").quantity(2).unitPrice(9000)
                .build();

        given(orderRepository.findBySessionIdAndDeletedFalseOrderByCreatedAtDesc(sessionId))
                .willReturn(List.of(order));
        given(orderItemRepository.findByOrderId(any())).willReturn(List.of(item));

        // when
        orderHistoryService.archiveSessionOrders(tableId, sessionId, storeId);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderHistoryRepository).saveAll(captor.capture());

        List<OrderHistory> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getOrderNumber()).isEqualTo("20260430-120000-ABCD");
        assertThat(saved.get(0).getTotalAmount()).isEqualTo(20000);
        assertThat(saved.get(0).getOrderItems()).contains("김치찌개");
    }

    @Test
    @DisplayName("이력 조회 - 날짜 필터 없이 전체 조회")
    void getTableHistory_noDateFilter() {
        // given
        Long tableId = 1L;
        OrderHistory history = OrderHistory.builder()
                .storeId(100L).tableId(tableId).sessionId(10L)
                .orderNumber("ORD-001").totalAmount(15000)
                .orderItems("[{\"menuName\":\"된장찌개\",\"quantity\":1,\"unitPrice\":8000}]")
                .orderedAt(LocalDateTime.now().minusHours(2))
                .completedAt(LocalDateTime.now().minusHours(1))
                .build();

        given(orderHistoryRepository.findByTableIdOrderByCompletedAtDesc(tableId))
                .willReturn(List.of(history));

        // when
        List<OrderHistoryResponse> result = orderHistoryService.getTableHistory(tableId, null, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-001");
        assertThat(result.get(0).getOrderItems()).hasSize(1);
        assertThat(result.get(0).getOrderItems().get(0).getMenuName()).isEqualTo("된장찌개");
    }

    @Test
    @DisplayName("이력 조회 - orderedAt 날짜 필터")
    void getTableHistory_orderedAtFilter() {
        // given
        Long tableId = 1L;
        LocalDate date = LocalDate.of(2026, 4, 30);

        given(orderHistoryRepository.findByTableIdAndOrderedAtBetweenOrderByOrderedAtDesc(
                eq(tableId), any(), any())).willReturn(List.of());

        // when
        List<OrderHistoryResponse> result = orderHistoryService.getTableHistory(tableId, date, "orderedAt");

        // then
        assertThat(result).isEmpty();
        verify(orderHistoryRepository).findByTableIdAndOrderedAtBetweenOrderByOrderedAtDesc(
                eq(tableId), any(), any());
    }

    @Test
    @DisplayName("이력 조회 - completedAt 날짜 필터")
    void getTableHistory_completedAtFilter() {
        // given
        Long tableId = 1L;
        LocalDate date = LocalDate.of(2026, 4, 30);

        given(orderHistoryRepository.findByTableIdAndCompletedAtBetweenOrderByCompletedAtDesc(
                eq(tableId), any(), any())).willReturn(List.of());

        // when
        List<OrderHistoryResponse> result = orderHistoryService.getTableHistory(tableId, date, "completedAt");

        // then
        assertThat(result).isEmpty();
        verify(orderHistoryRepository).findByTableIdAndCompletedAtBetweenOrderByCompletedAtDesc(
                eq(tableId), any(), any());
    }
}
