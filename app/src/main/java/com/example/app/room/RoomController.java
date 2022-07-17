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
import org.springframework.messaging.handler.annotation.DestinationVariable;
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

    @MessageMapping("/rooms/{roomCode}.join")
    @SendToUser(broadcast = false)
    public JoinRoomResponse joinRoom(UserName userName, @DestinationVariable String roomCode) {
        return new JoinRoomResponse(roomService.joinRoom(roomCode, userName.value()));
    }

    @MessageMapping("/rooms/{roomCode}/quiz.assign")
    @SendToUser(broadcast = false)
    public AssignQuizResponse assignQuiz(UserName userName, @DestinationVariable String roomCode, AssignQuizRequest request) {
        return new AssignQuizResponse(roomService.assignQuiz(userName.value(), roomCode, request.quizId()));
    }

    @MessageMapping("/rooms/{roomCode}/quiz.start")
    @SendToUser(broadcast = false)
    public StartQuizResponse startQuiz(UserName userName, @DestinationVariable String roomCode) {
        return new StartQuizResponse(ongoingQuizService.start(userName.value(), roomCode));
    }

    @MessageMapping("/rooms/{roomCode}/quiz.vote")
    @SendToUser(broadcast = false)
    public VoteResponse vote(UserName userName, @DestinationVariable String roomCode, VoteRequest request) {
        return new VoteResponse(ongoingQuizService.vote(userName.value(), roomCode, request.choice()));
    }

    @MessageMapping("/rooms/{roomCode}/quiz.move-on")
    @SendToUser(broadcast = false)
    public MoveOnResponse moveOn(UserName userName, @DestinationVariable String roomCode) {
        return new MoveOnResponse(ongoingQuizService.moveOn(userName.value(), roomCode));
    }

}
