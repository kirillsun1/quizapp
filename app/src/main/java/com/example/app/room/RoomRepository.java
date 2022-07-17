package com.example.app.room;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class RoomRepository {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public void save(Room room) {
        rooms.put(room.code(), room);
    }

    public Optional<Room> findByCode(String code) {
        return Optional.ofNullable(rooms.get(code));
    }

}
