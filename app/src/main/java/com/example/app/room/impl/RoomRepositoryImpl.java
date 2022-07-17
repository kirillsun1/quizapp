package com.example.app.room.impl;

import com.example.app.room.Room;
import com.example.app.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepository {

    private final MutableRoomRepository mutableRoomRepository;
    private final RoomMapper roomMapper;

    @Override
    public Optional<Room> findByCode(String code) {
        return mutableRoomRepository.findByCode(code)
                .map(roomMapper::toPublic);
    }

}
