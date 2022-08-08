package com.example.app.playscenarios;

import com.example.app.room.events.JoinRoomResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class RareScenariosTest extends AbstractPlayScenarioTest {

    @Test
    void doesNotJoinNotExistingRoom() {
        var moderator = new PlayerJoystick(UUID.randomUUID().toString());
        moderator.createSession();
        moderator.setRoom("012345");

        JoinRoomResponse response = moderator.joinRoom();

        assertThat(response.ok(), is(false));
    }

    @Test
    void doesNotSendResponsesToAnotherSession() {
        var moderator = new ModeratorJoystick(UUID.randomUUID().toString());
        var player = new PlayerJoystick(UUID.randomUUID().toString());
        var anotherPlayer = new PlayerJoystick(UUID.randomUUID().toString());

        moderator.createSession();
        player.createSession();
        anotherPlayer.createSession();

        var roomCode = moderator.createRoom().room().code();
        player.setRoom(roomCode);

        var anotherSessionJoinRoomResponseFuture =
                expectReply(anotherPlayer.session, "/user/queue/responses/rooms.join", JoinRoomResponse.class);

        JoinRoomResponse responseForPlayer = player.joinRoom();
        JoinRoomResponse responseForAnotherPlayer = getImmediatelyAndSafely(anotherSessionJoinRoomResponseFuture);

        assertThat(responseForPlayer, is(notNullValue()));
        assertThat(responseForAnotherPlayer, is(nullValue()));
    }

    private <T> T getImmediatelyAndSafely(Future<T> future) {
        try {
            future.cancel(true);
            return future.get();
        } catch (CancellationException | ExecutionException | InterruptedException ignored) {
            return null;
        }
    }

}
