package com.example.app.playscenarios;

import com.example.app.ongoingquiz.CurrentQuestion;
import com.example.app.ongoingquiz.OngoingQuizStatus;
import com.example.app.quiz.Quiz;
import com.example.app.quiz.QuizRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class OnePlayerSimpleTest extends AbstractPlayScenarioTest {

    private static final int QUIZ_ID = 999;

    private Quiz quiz;

    @Autowired
    private QuizRepository quizRepository;
    private ModeratorJoystick moderator;
    private PlayerJoystick player;

    @Test
    void simpleGame() throws Exception {
        moderator = new ModeratorJoystick(UUID.randomUUID().toString());
        player = new PlayerJoystick(UUID.randomUUID().toString());

        moderator.createSession();
        player.createSession();

        quiz = quizRepository.findById(QUIZ_ID).orElseThrow();

        String roomCode = moderatorCreatesRoom();

        player.setRoom(roomCode);
        playerJoinsRoom();

        moderatorAssignsQuiz();

        moderatorStartsQuiz();

        playerVotes(0);

        moderatorFinishesRound();

        playerAnswerWasCorrect();

        moderatorGoesToTheNextQuestion();

        playerVotes(1);

        moderatorFinishesRound();

        playerAnswerWasIncorrectAndQuizIsFinished();
    }

    private String moderatorCreatesRoom() {
        var createRoomResponse = moderator.createRoom();
        var room = createRoomResponse.room();
        assertThat(room, is(notNullValue()));
        assertThat(room.code(), is(notNullValue()));
        assertThat(room.players(), is(empty()));
        assertThat(room.ongoingQuiz(), is(nullValue()));

        moderator.setRoom(room.code());
        moderator.subscribeToRoomEvents();

        return room.code();
    }

    private void playerJoinsRoom() throws Exception {
        var joinRoomResponse = player.joinRoom();
        assertThat(joinRoomResponse.ok(), is(true));
        var room = joinRoomResponse.room();
        assertThat(room, is(notNullValue()));
        assertThat(room.code(), is(notNullValue()));
        assertThat(room.players(), contains(player.playerName));
        assertThat(room.ongoingQuiz(), is(nullValue()));

        player.subscribeToRoomEvents();

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        assertThat(moderatorRoomEvent, is(notNullValue()));
        assertThat(moderatorRoomEvent.room(), is(notNullValue()));
        assertThat(moderatorRoomEvent.room().players(), contains(player.playerName));
        assertThat(moderatorRoomEvent.room().ongoingQuiz(), is(nullValue()));

        assertThat(player.events.isEmpty(), is(true));
    }

    private void moderatorAssignsQuiz() throws Exception {
        var assignQuizResponse = moderator.assignQuiz(quiz.id());
        assertThat(assignQuizResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        assertThat(moderatorRoomEvent, is(notNullValue()));
        assertThat(moderatorRoomEvent.room(), is(notNullValue()));
        assertThat(moderatorRoomEvent.room().players(), containsInAnyOrder(player.playerName));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz, is(notNullValue()));
        assertThat(ongoingQuiz.currentQuestion(), is(nullValue()));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.NOT_STARTED));
        assertThat(ongoingQuiz.points(), is(aMapWithSize(1)));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 0));
    }

    private void moderatorStartsQuiz() throws Exception {
        var startQuizResponse = moderator.moveOn();
        assertThat(startQuizResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz, is(notNullValue()));
        CurrentQuestion currentQuestion = ongoingQuiz.currentQuestion();
        assertThat(currentQuestion, is(notNullValue()));
        assertThat(currentQuestion.text(), is(quiz.questions().get(0).text()));
        assertThat(currentQuestion.answers(), is(not(empty())));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.QUESTION_IN_PROGRESS));
    }

    private void playerVotes(int choice) throws Exception {
        var makeChoiceResponse = player.vote(choice);
        assertThat(makeChoiceResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        // no events expected
        assertThat(moderatorRoomEvent, is(nullValue()));
        assertThat(playerRoomEvent, is(nullValue()));
    }

    private void moderatorFinishesRound() {
        var finishRoundResponse = moderator.moveOn();
        assertThat(finishRoundResponse.ok(), is(true));
    }

    private void playerAnswerWasCorrect() throws Exception {
        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        assertThat(moderatorRoomEvent, is(notNullValue()));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz.currentQuestion(), is(nullValue()));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.WAITING));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 100));
    }

    private void moderatorGoesToTheNextQuestion() throws Exception {
        var finishRoundResponse = moderator.moveOn();
        assertThat(finishRoundResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        assertThat(moderatorRoomEvent, is(notNullValue()));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        CurrentQuestion currentQuestion = ongoingQuiz.currentQuestion();
        assertThat(currentQuestion, is(notNullValue()));
        assertThat(currentQuestion.text(), is(quiz.questions().get(1).text()));
        assertThat(currentQuestion.answers(), is(not(empty())));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.QUESTION_IN_PROGRESS));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 100));
    }

    private void playerAnswerWasIncorrectAndQuizIsFinished() throws Exception {
        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        assertThat(moderatorRoomEvent, is(notNullValue()));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz.currentQuestion(), is(nullValue()));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.DONE));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 75));
    }

}
