package com.tableorder.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class SseEmitterManager {

    @Value("${app.sse.timeout:1800000}")
    private long sseTimeout;

    @Value("${app.sse.max-emitters-per-store:10}")
    private int maxEmittersPerStore;

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitterMap =
            new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long storeId) {
        CopyOnWriteArrayList<SseEmitter> emitters =
                emitterMap.computeIfAbsent(storeId, k -> new CopyOnWriteArrayList<>());

        if (emitters.size() >= maxEmittersPerStore) {
            log.warn("매장 {}의 SSE 연결이 최대치({})에 도달했습니다.", storeId, maxEmittersPerStore);
            SseEmitter oldest = emitters.remove(0);
            oldest.complete();
        }

        SseEmitter emitter = new SseEmitter(sseTimeout);

        emitter.onCompletion(() -> removeEmitter(storeId, emitter));
        emitter.onTimeout(() -> removeEmitter(storeId, emitter));
        emitter.onError(e -> removeEmitter(storeId, emitter));

        emitters.add(emitter);
        log.debug("매장 {} SSE 연결 추가. 현재 연결 수: {}", storeId, emitters.size());

        return emitter;
    }

    public void sendEvent(Long storeId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = emitterMap.get(storeId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.debug("SSE 이벤트 전송 실패. 매장: {}, 이벤트: {}", storeId, eventName);
                removeEmitter(storeId, emitter);
            }
        }
    }

    public void removeEmitter(Long storeId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emitterMap.get(storeId);
        if (emitters != null) {
            emitters.remove(emitter);
            log.debug("매장 {} SSE 연결 제거. 현재 연결 수: {}", storeId, emitters.size());
            if (emitters.isEmpty()) {
                emitterMap.remove(storeId);
            }
        }
    }

    public int getEmitterCount(Long storeId) {
        CopyOnWriteArrayList<SseEmitter> emitters = emitterMap.get(storeId);
        return emitters != null ? emitters.size() : 0;
    }
}
