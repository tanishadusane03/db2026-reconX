package com.dbtraining.reconx.sse;

import com.dbtraining.reconx.dto.TradeResponse;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Backs GET /api/v1/trades/stream (Day 7 SSE live feed). Holds one open
 * SseEmitter per connected browser tab and fans every create/update/status
 * change out to all of them.
 */
@Component
public class TradeStreamService {

    private static final Logger log = LoggerFactory.getLogger(TradeStreamService.class);
    private static final long TIMEOUT_MS = 30L * 60 * 1000; // 30 minutes; client reconnects after.

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Tomcat doesn't flush response headers to the client until the first
        // byte of body is written, so without this EventSource.onopen never
        // fires until (or unless) a real trade event happens to come along.
        // A comment line is valid SSE, ignored by EventSource's onmessage, and
        // forces the headers out immediately.
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException ex) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcast(TradeResponse trade) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(trade, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException ex) {
                log.debug("Dropping dead SSE emitter: {}", ex.getMessage());
                emitters.remove(emitter);
            }
        }
    }
}
