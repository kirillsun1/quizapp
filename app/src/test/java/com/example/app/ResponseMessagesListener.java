package com.example.app;

import lombok.Getter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ResponseMessagesListener<T> implements StompFrameHandler {

    private static final int DEFAULT_EVENTS_QUEUE_CAPACITY = 100;

    @Getter
    private final BlockingQueue<T> queue = new ArrayBlockingQueue<>(DEFAULT_EVENTS_QUEUE_CAPACITY);
    private final Converter<byte[], T> converter;

    public ResponseMessagesListener(Converter<byte[], T> converter) {
        this.converter = converter;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return byte[].class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        queue.add(Objects.requireNonNull(converter.convert((byte[]) payload)));
    }

}
