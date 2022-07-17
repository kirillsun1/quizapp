package com.example.app.playscenarios;

import com.example.app.room.events.JoinRoomResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class RareScenariosTest extends AbstractPlayScenarioTest {

    @Test
    void doesNotJoinNotExistingRoom() {
        var moderator = new PlayerJoystick(DEFAULT_USER_NAME, session);
        moderator.setRoom("012345");

        JoinRoomResponse response = moderator.joinRoom();

        assertThat(response.ok(), is(false));
    }

    @Test
    void doesNotSendResponsesToAnotherSession() {
        var moderator = new ModeratorJoystick(DEFAULT_USER_NAME, session);
        var player = new PlayerJoystick("player", createSession("player"));
        var anotherPlayer = new PlayerJoystick("player2", createSession("player2"));

        var roomCode = moderator.createRoom().code();
        player.setRoom(roomCode);

        var anotherSessionJoinRoomResponseFuture =
                expectReply(anotherPlayer.session, "/user/queue/responses/rooms.join", JoinRoomResponse.class);

        JoinRoomResponse responseForPlayer = player.joinRoom();
        JoinRoomResponse responseForAnotherPlayer = getImmediatelyAndSafely(anotherSessionJoinRoomResponseFuture);

        assertThat(responseForPlayer, is(notNullValue()));
        assertThat(responseForAnotherPlayer, is(nullValue()));
    }

    private  <T> T getImmediatelyAndSafely(Future<T> future) {
        try {
            future.cancel(true);
            return future.get();
        } catch (CancellationException | ExecutionException | InterruptedException ignored) {
            return null;
        }
    }

}
