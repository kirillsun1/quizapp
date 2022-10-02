package com.example.app.game.impl;

import com.example.app.room.Room;
import com.example.app.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class RoomRepositoryImpl implements RoomRepository {

    private final LockingGameRepository lockingGameRepository;
    private final RoomMapper roomMapper;

    @Override
    public Optional<Room> findByCode(String code) {
        return lockingGameRepository.find(code).map(roomMapper::toPublic);
    }

}
