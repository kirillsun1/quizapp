package com.example.app.room.impl;


import com.example.app.room.OngoingQuizStatus;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MutableRoom {

    private final String code;

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

}
