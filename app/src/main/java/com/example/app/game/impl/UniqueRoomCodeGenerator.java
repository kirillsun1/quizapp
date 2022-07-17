package com.example.app.game.impl;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
class UniqueRoomCodeGenerator {

    private final AtomicInteger cur = new AtomicInteger(0);

    String generate() {
        return String.format("%06d", cur.incrementAndGet());
    }

}
