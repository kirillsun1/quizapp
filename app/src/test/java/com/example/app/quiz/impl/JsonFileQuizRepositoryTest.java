package com.example.app.quiz.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JsonFileQuizRepositoryTest {

    @Autowired
    private JsonFileQuizRepository quizRepository;

    @Test
    void findsQuizFromJson() {
        var quiz = quizRepository.findById(1).orElseThrow();

        assertThat(quiz.id(), is(1));
        assertThat(quiz.creator(), is("K"));
        assertThat(quiz.questions(), hasSize(1));
        var question = quiz.questions().get(0);
        assertThat(question.text(), is("?"));
        assertThat(question.answers(), hasSize(2));
        var answer = question.answers().get(0);
        assertThat(answer.text(), is("1"));
        assertThat(answer.points(), is(1000));
        answer = question.answers().get(1);
        assertThat(answer.text(), is("2"));
        assertThat(answer.points(), is(-1000));
    }
}