package com.example.app.room;

import com.example.app.core.BaseResponse;
import com.example.app.core.UserName;
import com.example.app.room.events.AssignQuizRequest;
import com.example.app.room.events.CreateRoomResponse;
import com.example.app.room.events.JoinRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
        return new CreateRoomResponse(roomService.createRoom(userName.value()));
    }

    @MessageMapping("/rooms/{roomCode}.join")
    @SendToUser(broadcast = false)
    public JoinRoomResponse joinRoom(UserName userName, @DestinationVariable String roomCode) {
        return new JoinRoomResponse(roomService.joinRoom(roomCode, userName.value()));
    }

    @MessageMapping("/rooms/{roomCode}/quiz.assign")
    @SendToUser(broadcast = false)
    public BaseResponse assignQuiz(UserName userName, @DestinationVariable String roomCode, AssignQuizRequest request) {
        roomService.assignQuiz(userName.value(), roomCode, request.quizId());
        return BaseResponse.ok();
    }

}
