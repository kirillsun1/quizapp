package com.example.app.quiz.impl;

import com.example.app.quiz.Quiz;
import com.example.app.quiz.QuizRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Repository
public class JsonFileQuizRepository implements QuizRepository {

    private static final String DB_FILE_PATH = "/quizdb.json";

    private final Map<Integer, Quiz> cache;

    public JsonFileQuizRepository(ObjectMapper objectMapper) {
        cache = loadQuizzes(objectMapper);
        log.info("{} quizzes loaded.", cache.size());
    }

    private Map<Integer, Quiz> loadQuizzes(ObjectMapper objectMapper) {
        try (InputStream resourceStream = getClass().getResourceAsStream(DB_FILE_PATH)) {
            if (resourceStream == null) {
                throw new FileNotFoundException(DB_FILE_PATH + " not found.");
            }
            return objectMapper
                    .readValue(resourceStream, new TypeReference<List<Quiz>>() {
                    })
                    .stream()
                    .collect(toMap(Quiz::id, Function.identity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Quiz> findById(int id) {
        return Optional.ofNullable(cache.get(id));
    }

}
