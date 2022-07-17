package com.example.app.room;

import java.util.Optional;


public interface RoomRepository {

    Optional<Room> findByCode(String code);

}
