package com.example.app.quiz;

import lombok.Builder;

import java.util.List;

public record Question(String text, List<Answer> answers) {
    @Builder
    public Question {
    }
}
