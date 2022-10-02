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

    Room toPublic(Game game) {
        var builder = Room.builder()
                .code(game.getRoomCode())
                .moderator(game.getModerator())
                .players(game.getPlayersPoints().keySet());
        var quiz = findQuiz(game.getQuizId());
        if (quiz != null) {
            builder.ongoingQuiz(OngoingQuiz.builder()
                    .currentQuestion(buildCurrentQuestion(quiz, game))
                    .points(game.getPlayersPoints())
                    .status(game.getStatus())
                    .build());
        }
        return builder.build();
    }

    private CurrentQuestion buildCurrentQuestion(Quiz quiz, Game game) {
        if (!game.showQuestion()) {
            return null;
        }
        var currentQuestion = game.getCurrentQuestion();
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
