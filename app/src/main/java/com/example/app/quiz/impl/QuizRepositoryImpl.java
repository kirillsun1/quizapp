package com.example.app.quiz.impl;

import com.example.app.quiz.Answer;
import com.example.app.quiz.Question;
import com.example.app.quiz.Quiz;
import com.example.app.quiz.QuizRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class QuizRepositoryImpl implements QuizRepository {

    private final Map<Integer, Quiz> cache = new ConcurrentHashMap<>();

    public QuizRepositoryImpl() {
        cache.put(10, Quiz.builder()
                .id(10)
                .creator("Kirill test")
                .questions(List.of(
                        Question.builder()
                                .text("2 + 2 * 2")
                                .answers(List.of(new Answer("6", 1000), new Answer("8", 0)))
                                .build(),
                        Question.builder()
                                .text("Can I have a tea?")
                                .answers(List.of(new Answer("Yes", 0), new Answer("No", -1000)))
                                .build()
                ))
                .build());
    }

    @Override
    public Optional<Quiz> findById(int id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public void save(Quiz quiz) {
        cache.put(quiz.id(), quiz);
    }

}
