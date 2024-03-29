package com.example.app.room;

import com.example.app.ongoingquiz.OngoingQuiz;
import lombok.Builder;

import java.util.Set;

public record Room(String code, OngoingQuiz ongoingQuiz, String moderator, Set<String> players) {
    @Builder
    public Room {
    }
}
