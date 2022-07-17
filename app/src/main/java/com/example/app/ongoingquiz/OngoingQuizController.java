package com.example.app.ongoingquiz;

import com.example.app.core.BaseGameException;
import com.example.app.core.UserName;
import com.example.app.ongoingquiz.events.MoveOnResponse;
import com.example.app.ongoingquiz.events.VoteRequest;
import com.example.app.ongoingquiz.events.VoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class OngoingQuizController {

    private final OngoingQuizService ongoingQuizService;

    @MessageMapping("/rooms/{roomCode}/quiz.vote")
    @SendToUser(broadcast = false)
    public VoteResponse vote(UserName userName, @DestinationVariable String roomCode, VoteRequest request) {
        try {
            ongoingQuizService.vote(userName.value(), roomCode, request.choice());
            return new VoteResponse(true);
        } catch (BaseGameException ex) {
            return new VoteResponse(false);
        }
    }

    @MessageMapping("/rooms/{roomCode}/quiz.move-on")
    @SendToUser(broadcast = false)
    public MoveOnResponse moveOn(UserName userName, @DestinationVariable String roomCode) {
        try {
            ongoingQuizService.moveOn(userName.value(), roomCode);
            return new MoveOnResponse(true);
        } catch (BaseGameException ex) {
            return new MoveOnResponse(false);
        }
    }

}
