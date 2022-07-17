package com.example.app.quiz;

import lombok.Builder;

import java.util.List;

public record Quiz(int id,
                   String creator,
                   List<Question> questions) {
    @Builder
    public Quiz {
    }
}

