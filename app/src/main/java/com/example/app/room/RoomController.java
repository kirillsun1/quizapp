package com.example.app.room;

import com.example.app.config.UserName;
import com.example.app.room.events.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @MessageMapping("/rooms.create")
    @SendToUser(broadcast = false)
    public CreateRoomResponse createRoom(UserName userName) {
        return new CreateRoomResponse(roomService.createRoom(userName.value()).code());
    }

    @MessageMapping("/rooms.join")
    @SendToUser(broadcast = false)
    public JoinRoomResponse joinRoom(UserName userName, JoinRoomRequest request) {
        var ok = roomService.joinRoom(request.code(), userName.value());
        return new JoinRoomResponse(ok);
    }

    @MessageMapping("/rooms.assign-quiz")
    @SendToUser(broadcast = false)
    public AssignQuizResponse assignQuiz(UserName userName, AssignQuizRequest request) {
        var ok = roomService.assignQuiz(userName.value(), request.roomCode(), request.quizId());
        return new AssignQuizResponse(ok);
    }

}
