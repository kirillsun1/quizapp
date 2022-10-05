package com.example.app.game.impl;

import com.example.app.ongoingquiz.OngoingQuizStatus;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
class Game {

    private final String roomCode;

    private Integer quizId;

    private String moderator;

    @Builder.Default
    private Map<String, Integer> playersPoints = new HashMap<>();

    @Builder.Default
    private OngoingQuizStatus status = OngoingQuizStatus.NOT_STARTED;

    @Builder.Default
    private int currentQuestion = 0;

    @Builder.Default
    private Map<Integer, Map<String, Integer>> votesByQuestions = new HashMap<>();

    public boolean showQuestion() {
        return status == OngoingQuizStatus.QUESTION_IN_PROGRESS;
    }


}
