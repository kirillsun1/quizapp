package com.example.app.room;

import com.example.app.config.UserName;
import com.example.app.room.events.CreateRoomResponse;
import com.example.app.room.events.JoinRoomRequest;
import com.example.app.room.events.JoinRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @MessageMapping("/rooms.create")
    @SendToUser(value = "/queue/responses/rooms.create", broadcast = false)
    public CreateRoomResponse createRoom(UserName userName) {
        return new CreateRoomResponse(roomService.createRoom(userName.value()).code());
    }

    @MessageMapping("/rooms.join")
    @SendToUser(value = "/queue/responses/rooms.join", broadcast = false)
    public JoinRoomResponse joinRoom(UserName userName, JoinRoomRequest request) {
        return roomService.joinRoom(request.code(), userName.value())
                .map(room -> new JoinRoomResponse(true))
                .orElseGet(() -> new JoinRoomResponse(false));
    }

}
