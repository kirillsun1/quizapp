package com.example.app.room.impl;


import com.example.app.room.OngoingQuizStatus;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class MutableRoom {

    private final String code;

    private Integer quizId;

    private String moderator;

    @Builder.Default
    private Map<String, Integer> playersPoints = new HashMap<>();


    // TODO: decouple from ongoing quiz status?
    @Builder.Default
    private OngoingQuizStatus status = OngoingQuizStatus.NOT_STARTED;

    @Builder.Default
    private int currentQuestion = 0;

}
