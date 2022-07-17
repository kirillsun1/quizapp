package com.example.app.quiz;

import lombok.Builder;

import java.util.List;

public record Question(String text,
                       List<String> answers,
                       List<Integer> correctAnswersIndexes,
                       int correctPoints,
                       int incorrectPoints) {
    @Builder
    public Question {
    }
}
