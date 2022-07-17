package com.example.app.game.impl;

import com.example.app.ongoingquiz.OngoingQuiz;
import com.example.app.quiz.Quiz;
import com.example.app.quiz.QuizRepository;
import com.example.app.room.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RoomMapper {

    private final QuizRepository quizRepository;

    Room toPublic(MutableRoom mutableRoom) {
        var builder = Room.builder()
                .code(mutableRoom.getCode())
                .moderator(mutableRoom.getModerator())
                .players(mutableRoom.getPlayersPoints().keySet());
        var quiz = findQuiz(mutableRoom.getQuizId());
        if (quiz != null) {
            builder.ongoingQuiz(OngoingQuiz.builder()
                    .quiz(quiz)
                    .currentQuestion(mutableRoom.getCurrentQuestion())
                    .points(mutableRoom.getPlayersPoints())
                    .status(mutableRoom.getStatus())
                    .build());
        }
        return builder.build();
    }

    private Quiz findQuiz(Integer quizId) {
        if (quizId == null) {
            return null;
        }
        return quizRepository.findById(quizId).orElse(null);
    }

}