package com.tableorder.service;

import com.tableorder.dto.OrderEventData;
import com.tableorder.infrastructure.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventService {

    private final SseEmitterManager sseEmitterManager;

    public SseEmitter subscribe(Long storeId) {
        SseEmitter emitter = sseEmitterManager.createEmitter(storeId);
        log.info("매장 {} SSE 구독 등록. 현재 연결 수: {}", storeId, sseEmitterManager.getEmitterCount(storeId));
        return emitter;
    }

    public void publishOrderCreated(Long storeId, OrderEventData eventData) {
        log.debug("매장 {} 주문 생성 이벤트 발행: {}", storeId, eventData.getOrderNumber());
        sseEmitterManager.sendEvent(storeId, "order-created", eventData);
    }

    public void publishOrderStatusChanged(Long storeId, OrderEventData eventData) {
        log.debug("매장 {} 주문 상태 변경 이벤트 발행: {} -> {}", storeId, eventData.getOrderNumber(), eventData.getStatus());
        sseEmitterManager.sendEvent(storeId, "order-status-changed", eventData);
    }

    public void publishOrderDeleted(Long storeId, OrderEventData eventData) {
        log.debug("매장 {} 주문 삭제 이벤트 발행: {}", storeId, eventData.getOrderNumber());
        sseEmitterManager.sendEvent(storeId, "order-deleted", eventData);
    }
}
