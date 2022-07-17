package com.example.app.room;

import com.example.app.config.UserName;
import com.example.app.room.events.AssignQuizRequest;
import com.example.app.room.events.AssignQuizResponse;
import com.example.app.room.events.CreateRoomResponse;
import com.example.app.room.events.JoinRoomRequest;
import com.example.app.room.events.JoinRoomResponse;
import com.example.app.room.events.MoveOnRequest;
import com.example.app.room.events.MoveOnResponse;
import com.example.app.room.events.StartQuizRequest;
import com.example.app.room.events.StartQuizResponse;
import com.example.app.room.events.VoteRequest;
import com.example.app.room.events.VoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final OngoingQuizService ongoingQuizService;

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
        return new StartQuizResponse(ongoingQuizService.start(userName.value(), request.roomCode()));
    }

    @MessageMapping("/rooms.quiz.vote")
    @SendToUser(broadcast = false)
    public VoteResponse vote(UserName userName, VoteRequest request) {
        return new VoteResponse(ongoingQuizService.vote(userName.value(), request.roomCode(), request.choice()));
    }

    @MessageMapping("/rooms.quiz.move-on")
    @SendToUser(broadcast = false)
    public MoveOnResponse moveOn(UserName userName, MoveOnRequest request) {
        return new MoveOnResponse(ongoingQuizService.moveOn(userName.value(), request.code()));
    }

}
