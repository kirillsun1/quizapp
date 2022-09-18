package com.example.app.ongoingquiz;

import com.example.app.core.BaseResponse;
import com.example.app.core.UserName;
import com.example.app.ongoingquiz.events.VoteRequest;
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
    public BaseResponse vote(UserName userName, @DestinationVariable String roomCode, VoteRequest request) {
        ongoingQuizService.vote(userName.value(), roomCode, request.choice());
        return BaseResponse.ok();
    }

    @MessageMapping("/rooms/{roomCode}/quiz.move-on")
    @SendToUser(broadcast = false)
    public BaseResponse moveOn(UserName userName, @DestinationVariable String roomCode) {
        ongoingQuizService.moveOn(userName.value(), roomCode);
        return BaseResponse.ok();
    }

}
