package com.tableorder.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class SseEmitterManagerTest {

    private SseEmitterManager sseEmitterManager;

    @BeforeEach
    void setUp() {
        sseEmitterManager = new SseEmitterManager();
        ReflectionTestUtils.setField(sseEmitterManager, "sseTimeout", 1800000L);
        ReflectionTestUtils.setField(sseEmitterManager, "maxEmittersPerStore", 3);
    }

    @Test
    @DisplayName("SSE Emitter 생성 시 카운트 증가")
    void testCreateEmitter() {
        Long storeId = 1L;

        SseEmitter emitter = sseEmitterManager.createEmitter(storeId);

        assertThat(emitter).isNotNull();
        assertThat(sseEmitterManager.getEmitterCount(storeId)).isEqualTo(1);
    }

    @Test
    @DisplayName("SSE Emitter 제거 시 카운트 감소")
    void testRemoveEmitter() {
        Long storeId = 1L;

        SseEmitter emitter = sseEmitterManager.createEmitter(storeId);
        sseEmitterManager.removeEmitter(storeId, emitter);

        assertThat(sseEmitterManager.getEmitterCount(storeId)).isEqualTo(0);
    }

    @Test
    @DisplayName("매장당 최대 Emitter 수 초과 시 가장 오래된 것 제거")
    void testMaxEmittersPerStore() {
        Long storeId = 1L;

        sseEmitterManager.createEmitter(storeId);
        sseEmitterManager.createEmitter(storeId);
        sseEmitterManager.createEmitter(storeId);
        sseEmitterManager.createEmitter(storeId); // 4th - should trigger removal of oldest

        assertThat(sseEmitterManager.getEmitterCount(storeId)).isEqualTo(3);
    }
}
