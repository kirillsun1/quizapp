package com.example.app.game.impl;

import com.example.app.ongoingquiz.CurrentQuestion;
import com.example.app.ongoingquiz.OngoingQuiz;
import com.example.app.quiz.Answer;
import com.example.app.quiz.Quiz;
import com.example.app.quiz.QuizRepository;
import com.example.app.room.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.stream.Collectors;

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
                    .currentQuestion(buildCurrentQuestion(quiz, mutableRoom))
                    .points(mutableRoom.getPlayersPoints())
                    .status(mutableRoom.getStatus())
                    .build());
        }
        return builder.build();
    }

    private CurrentQuestion buildCurrentQuestion(Quiz quiz, MutableRoom mutableRoom) {
        if (!mutableRoom.showQuestion()) {
            return null;
        }
        var currentQuestion = mutableRoom.getCurrentQuestion();
        Assert.isTrue(
                currentQuestion >= 0 && currentQuestion < quiz.questions().size(),
                "Quiz[id=" + quiz.id() + "] doesn't have question with index " + currentQuestion);
        var question = quiz.questions().get(currentQuestion);
        var textAnswers = question.answers().stream().map(Answer::text).collect(Collectors.toList());
        return new CurrentQuestion(question.text(), textAnswers);
    }

    private Quiz findQuiz(Integer quizId) {
        if (quizId == null) {
            return null;
        }
        return quizRepository.findById(quizId).orElse(null);
    }

}
