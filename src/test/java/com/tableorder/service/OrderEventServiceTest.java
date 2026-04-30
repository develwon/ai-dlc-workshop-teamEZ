package com.tableorder.service;

import com.tableorder.dto.OrderEventData;
import com.tableorder.infrastructure.SseEmitterManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventServiceTest {

    @InjectMocks
    private OrderEventService orderEventService;

    @Mock
    private SseEmitterManager sseEmitterManager;

    private static final Long STORE_ID = 1L;

    @Test
    @DisplayName("SSE 구독 등록 성공")
    void subscribe_success() {
        // given
        SseEmitter emitter = new SseEmitter();
        given(sseEmitterManager.createEmitter(STORE_ID)).willReturn(emitter);
        given(sseEmitterManager.getEmitterCount(STORE_ID)).willReturn(1);

        // when
        SseEmitter result = orderEventService.subscribe(STORE_ID);

        // then
        assertThat(result).isEqualTo(emitter);
        then(sseEmitterManager).should().createEmitter(STORE_ID);
    }

    @Test
    @DisplayName("주문 생성 이벤트 발행")
    void publishOrderCreated_success() {
        // given
        OrderEventData eventData = OrderEventData.builder()
                .eventType("ORDER_CREATED")
                .orderId(1L)
                .orderNumber("20260430-120000-ABCD")
                .tableId(1L)
                .tableNumber(1)
                .totalAmount(18000)
                .status("PENDING")
                .timestamp(LocalDateTime.now())
                .build();

        // when
        orderEventService.publishOrderCreated(STORE_ID, eventData);

        // then
        then(sseEmitterManager).should().sendEvent(STORE_ID, "order-created", eventData);
    }

    @Test
    @DisplayName("주문 상태 변경 이벤트 발행")
    void publishOrderStatusChanged_success() {
        // given
        OrderEventData eventData = OrderEventData.builder()
                .eventType("ORDER_STATUS_CHANGED")
                .orderId(1L)
                .orderNumber("20260430-120000-ABCD")
                .status("PREPARING")
                .timestamp(LocalDateTime.now())
                .build();

        // when
        orderEventService.publishOrderStatusChanged(STORE_ID, eventData);

        // then
        then(sseEmitterManager).should().sendEvent(STORE_ID, "order-status-changed", eventData);
    }

    @Test
    @DisplayName("주문 삭제 이벤트 발행")
    void publishOrderDeleted_success() {
        // given
        OrderEventData eventData = OrderEventData.builder()
                .eventType("ORDER_DELETED")
                .orderId(1L)
                .orderNumber("20260430-120000-ABCD")
                .timestamp(LocalDateTime.now())
                .build();

        // when
        orderEventService.publishOrderDeleted(STORE_ID, eventData);

        // then
        then(sseEmitterManager).should().sendEvent(STORE_ID, "order-deleted", eventData);
    }
}
