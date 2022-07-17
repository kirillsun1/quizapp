package com.example.app.quiz.impl;

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

    @Override
    public Optional<Quiz> findById(int id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public void save(Quiz quiz) {
        cache.put(quiz.id(), quiz);
    }

}
