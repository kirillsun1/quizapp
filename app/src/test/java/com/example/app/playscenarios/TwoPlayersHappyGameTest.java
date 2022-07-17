package com.example.app.playscenarios;

import com.example.app.ongoingquiz.OngoingQuizStatus;
import com.example.app.quiz.Answer;
import com.example.app.quiz.Question;
import com.example.app.quiz.Quiz;
import com.example.app.quiz.QuizRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class TwoPlayersHappyGameTest extends AbstractPlayScenarioTest {

    private static final Quiz quiz = Quiz.builder()
            .id(999)
            .creator("this person")
            .questions(List.of(
                    Question.builder()
                            .text("Yes?")
                            .answers(List.of(
                                    new Answer("Yes", 100),
                                    new Answer("No", 0)))
                            .build(),
                    Question.builder()
                            .text("Is that true?")
                            .answers(List.of(
                                    new Answer("Yes", 50),
                                    new Answer("No", -25),
                                    new Answer("Maybe", 1)))
                            .build()))
            .build();

    @Autowired
    private QuizRepository quizRepository;
    private ModeratorJoystick moderator;
    private PlayerJoystick player;
    private PlayerJoystick anotherPlayer;

    @Test
    void twoPlayersHappy() throws Exception {
        moderator = new ModeratorJoystick(DEFAULT_USER_NAME, session);
        player = new PlayerJoystick("player 1", createSession("player 1"));
        anotherPlayer = new PlayerJoystick("player 2", createSession("player 2"));

        quizRepository.save(quiz);

        String roomCode = moderatorCreatesRoom();

        player.setRoom(roomCode);
        anotherPlayer.setRoom(roomCode);

        playerJoinsRoom(player);
        moderator.events.clear(); // not relevant. almost the same as the next one

        playerJoinsRoom(anotherPlayer);
        player.events.clear();

        moderatorGetsCorrectEvent();

        moderatorAssignsQuiz();

        moderatorStartsQuiz();

        playerVotes(anotherPlayer, 0);
        playerVotes(player, 1);

        moderatorFinishesRound();

        pointsAreCorrect();

        moderatorGoesToTheNextQuestion();

        playerVotes(player, 2);
        playerVotes(anotherPlayer, 0);

        moderatorFinishesRound();

        pointsAreCorrectQuizIsFinished();
    }

    private String moderatorCreatesRoom() {
        var createRoomResponse = moderator.createRoom();
        var roomCode = createRoomResponse.code();
        assertThat(roomCode, is(notNullValue()));

        moderator.setRoom(roomCode);
        moderator.subscribeToRoomEvents();

        return roomCode;
    }

    private void playerJoinsRoom(PlayerJoystick player) {
        var joinRoomResponse = player.joinRoom();
        assertThat(joinRoomResponse.ok(), is(true));

        player.subscribeToRoomEvents();
        assertThat(player.events.isEmpty(), is(true));
    }

    private void moderatorGetsCorrectEvent() throws Exception {
        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        assertThat(moderatorRoomEvent, is(notNullValue()));
        assertThat(moderatorRoomEvent.room(), is(notNullValue()));
        assertThat(moderatorRoomEvent.room().players(), containsInAnyOrder(player.playerName, anotherPlayer.playerName));
        assertThat(moderatorRoomEvent.room().ongoingQuiz(), is(nullValue()));
    }

    private void moderatorAssignsQuiz() throws Exception {
        var assignQuizResponse = moderator.assignQuiz(quiz.id());
        assertThat(assignQuizResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var anotherPlayerRoomEvent = anotherPlayer.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));
        assertThat(moderatorRoomEvent, is(anotherPlayerRoomEvent));

        assertThat(moderatorRoomEvent, is(notNullValue()));
        assertThat(moderatorRoomEvent.room(), is(notNullValue()));
        assertThat(moderatorRoomEvent.room().players(), containsInAnyOrder(player.playerName, anotherPlayer.playerName));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz, is(notNullValue()));
        assertThat(ongoingQuiz.currentQuestion(), is(0));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.NOT_STARTED));
        assertThat(ongoingQuiz.points(), is(aMapWithSize(2)));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 0));
        assertThat(ongoingQuiz.points(), hasEntry(anotherPlayer.playerName, 0));
        assertThat(ongoingQuiz.quiz(), is(quiz));
    }

    private void moderatorStartsQuiz() throws Exception {
        var startQuizResponse = moderator.moveOn();
        assertThat(startQuizResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var anotherPlayerRoomEvent = anotherPlayer.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));
        assertThat(moderatorRoomEvent, is(anotherPlayerRoomEvent));

        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz, is(notNullValue()));
        assertThat(ongoingQuiz.currentQuestion(), is(0));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.QUESTION_IN_PROGRESS));
    }

    private void playerVotes(PlayerJoystick player, int choice) throws Exception {
        var makeChoiceResponse = player.vote(choice);
        assertThat(makeChoiceResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var anotherPlayerRoomEvent = anotherPlayer.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        // no events expected
        assertThat(moderatorRoomEvent, is(nullValue()));
        assertThat(playerRoomEvent, is(nullValue()));
        assertThat(anotherPlayerRoomEvent, is(nullValue()));
    }

    private void moderatorFinishesRound() {
        var finishRoundResponse = moderator.moveOn();
        assertThat(finishRoundResponse.ok(), is(true));
    }

    private void pointsAreCorrect() throws Exception {
        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var anotherPlayerRoomEvent = anotherPlayer.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));
        assertThat(moderatorRoomEvent, is(anotherPlayerRoomEvent));

        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz.currentQuestion(), is(0));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.WAITING));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 0));
        assertThat(ongoingQuiz.points(), hasEntry(anotherPlayer.playerName, 100));
    }

    private void moderatorGoesToTheNextQuestion() throws Exception {
        var finishRoundResponse = moderator.moveOn();
        assertThat(finishRoundResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var anotherPlayerRoomEvent = anotherPlayer.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));
        assertThat(moderatorRoomEvent, is(anotherPlayerRoomEvent));

        assertThat(moderatorRoomEvent, is(notNullValue()));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz.currentQuestion(), is(1));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.QUESTION_IN_PROGRESS));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 0));
        assertThat(ongoingQuiz.points(), hasEntry(anotherPlayer.playerName, 100));
    }

    private void pointsAreCorrectQuizIsFinished() throws Exception {
        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var anotherPlayerRoomEvent = anotherPlayer.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));
        assertThat(moderatorRoomEvent, is(anotherPlayerRoomEvent));

        assertThat(moderatorRoomEvent, is(notNullValue()));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz.currentQuestion(), is(1));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.DONE));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 1));
        assertThat(ongoingQuiz.points(), hasEntry(anotherPlayer.playerName, 150));
    }


}
