package com.example.app.quiz;

import lombok.Builder;

import java.util.Map;

public record OngoingQuiz(Quiz quiz,
                          int currentQuestion,
                          Map<String, Integer> points,
                          OngoingQuizStatus status) {
    @Builder
    public OngoingQuiz {
    }
}
