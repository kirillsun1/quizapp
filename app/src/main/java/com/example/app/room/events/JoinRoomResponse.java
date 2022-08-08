package com.example.app.room.events;

import com.example.app.room.Room;

public record JoinRoomResponse(boolean ok, Room room) {
}
