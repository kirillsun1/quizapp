package com.example.app.room;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UniqueRoomCodeGenerator {

    private final AtomicInteger cur = new AtomicInteger(0);

    public String generate() {
        return String.format("%06d", cur.incrementAndGet());
    }

}
