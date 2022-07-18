package com.example.app.ongoingquiz;

import lombok.Builder;

import java.util.Map;

public record OngoingQuiz(CurrentQuestion currentQuestion,
                          Map<String, Integer> points,
                          OngoingQuizStatus status) {
    @Builder
    public OngoingQuiz {
    }
}
