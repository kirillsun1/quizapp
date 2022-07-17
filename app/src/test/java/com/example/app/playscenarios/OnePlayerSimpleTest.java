package com.example.app.playscenarios;

import com.example.app.room.OngoingQuizStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class OnePlayerSimpleTest extends AbstractPlayScenarioTest {

    private ModeratorJoystick moderator;
    private PlayerJoystick player;

    @Test
    void simpleGame() throws Exception {
        // given
        moderator = new ModeratorJoystick(DEFAULT_USER_NAME, session);
        player = new PlayerJoystick("SomePlayer", createSession("SomePlayer"));

        String roomCode = moderatorCreatesRoom();

        player.setRoom(roomCode);
        playerJoinsRoom();

        moderatorAssignsQuiz();

        moderatorStartsQuiz();

        playerVotes();

        moderatorFinishesRound();

        playerAnswerWasCorrect();
    }

    private String moderatorCreatesRoom() {
        var createRoomResponse = moderator.createRoom();
        var roomCode = createRoomResponse.code();
        assertThat(roomCode, is(notNullValue()));

        moderator.setRoom(roomCode);
        moderator.subscribeToRoomEvents();

        return roomCode;
    }

    private void playerJoinsRoom() throws Exception {
        var joinRoomResponse = player.joinRoom();
        assertThat(joinRoomResponse.ok(), is(true));

        player.subscribeToRoomEvents();

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        assertThat(moderatorRoomEvent, is(notNullValue()));
        assertThat(moderatorRoomEvent.room(), is(notNullValue()));
        assertThat(moderatorRoomEvent.room().players(), containsInAnyOrder(player.playerName));
        assertThat(moderatorRoomEvent.room().ongoingQuiz(), is(nullValue()));

        assertThat(player.events.isEmpty(), is(true));
    }

    private void moderatorAssignsQuiz() throws Exception {
        var assignQuizResponse = moderator.assignQuiz();
        assertThat(assignQuizResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        assertThat(moderatorRoomEvent, is(notNullValue()));
        assertThat(moderatorRoomEvent.room(), is(notNullValue()));
        assertThat(moderatorRoomEvent.room().players(), containsInAnyOrder(player.playerName));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz, is(notNullValue()));
        assertThat(ongoingQuiz.currentQuestion(), is(0));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.NOT_STARTED));
        assertThat(ongoingQuiz.points(), is(aMapWithSize(1)));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 0));
        assertThat(ongoingQuiz.quiz(), is(notNullValue()));
    }

    private void moderatorStartsQuiz() throws Exception {
        var startQuizResponse = moderator.startQuiz();
        assertThat(startQuizResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz, is(notNullValue()));
        assertThat(ongoingQuiz.currentQuestion(), is(0));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.QUESTION_IN_PROGRESS));
    }

    private void playerVotes() throws InterruptedException {
        var makeChoiceResponse = player.vote(2);
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
        assertThat(ongoingQuiz.currentQuestion(), is(0));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.WAITING));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 100));
    }

}
