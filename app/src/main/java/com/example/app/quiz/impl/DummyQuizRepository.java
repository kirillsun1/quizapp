package com.example.app.quiz.impl;

import com.example.app.quiz.Quiz;
import com.example.app.quiz.QuizRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DummyQuizRepository implements QuizRepository {
    @Override
    public Optional<Quiz> findById(int id) {
        if (id < 1) {
            return Optional.empty();
        }
        return Optional.of(Quiz.builder().id(id)
                .creator("somebody")
                .questions(List.of())
                .build());
    }

}
