package com.example.app.game.impl;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
class MutableRoomRepository {

    private final Map<String, MutableRoom> rooms = new ConcurrentHashMap<>();

    public void save(MutableRoom room) {
        rooms.put(room.getCode(), room);
    }

    public Optional<MutableRoom> findByCode(String code) {
        return Optional.ofNullable(rooms.get(code));
    }


}
