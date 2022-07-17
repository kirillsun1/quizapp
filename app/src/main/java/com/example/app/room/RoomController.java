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
        return new JoinRoomResponse(roomService.joinRoom(request.code(), userName.value()));
    }

    @MessageMapping("/rooms.quiz.assign")
    @SendToUser(broadcast = false)
    public AssignQuizResponse assignQuiz(UserName userName, AssignQuizRequest request) {
        return new AssignQuizResponse(roomService.assignQuiz(userName.value(), request.roomCode(), request.quizId()));
    }

    @MessageMapping("/rooms.quiz.start")
    @SendToUser(broadcast = false)
    public StartQuizResponse startQuiz(UserName userName, StartQuizRequest request) {
        return new StartQuizResponse(roomService.startQuiz(userName.value(), request.roomCode()));
    }

    @MessageMapping("/rooms.quiz.vote")
    @SendToUser(broadcast = false)
    public VoteResponse vote(UserName userName, VoteRequest request) {
        return new VoteResponse(roomService.vote(userName.value(), request.roomCode(), request.choice()));
    }

    @MessageMapping("/rooms.quiz.move-on")
    @SendToUser(broadcast = false)
    public MoveOnResponse moveOn(UserName userName, MoveOnRequest request) {
        return new MoveOnResponse(roomService.moveOn(userName.value(), request.code()));
    }

}
