package com.example.app.quiz;

import java.util.Optional;

public interface QuizRepository {

    Optional<Quiz> findById(int id);

    void save(Quiz quiz);
}
