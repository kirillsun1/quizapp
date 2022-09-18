package com.example.app.room.events;

import com.example.app.core.BaseResponse;
import com.example.app.room.Room;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public final class JoinRoomResponse extends BaseResponse {
    private final Room room;

    @JsonCreator
    public JoinRoomResponse(@JsonProperty Room room) {
        super(false);
        this.room = room;
    }
}
