package com.example.app;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor
public class EventsListener implements StompFrameHandler {

    private static final int DEFAULT_EVENTS_QUEUE_CAPACITY = 100;

    @Getter
    private final BlockingQueue<byte[]> events = new ArrayBlockingQueue<>(DEFAULT_EVENTS_QUEUE_CAPACITY);

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return byte[].class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        events.add((byte[]) payload);
    }

}
