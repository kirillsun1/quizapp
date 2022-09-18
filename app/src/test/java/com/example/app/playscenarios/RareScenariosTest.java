package com.example.app.playscenarios;

import com.example.app.EventsListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RareScenariosTest extends AbstractPlayScenarioTest {

    @Test
    void doesNotJoinNotExistingRoom() {
        var moderator = new PlayerJoystick(UUID.randomUUID().toString());
        moderator.createSession();
        moderator.setRoom("012345");

        Executable executable = moderator::joinRoom;

        var exception = assertThrows(RuntimeException.class, executable);
        assertThat(exception.getMessage(), containsString("RoomNotFound"));
    }

    @Test
    void doesNotSendResponsesToAnotherSession() {
        var moderator = new ModeratorJoystick(UUID.randomUUID().toString());
        var player = new PlayerJoystick(UUID.randomUUID().toString());
        var anotherPlayer = new PlayerJoystick(UUID.randomUUID().toString());

        moderator.createSession();
        player.createSession();
        anotherPlayer.createSession();

        var moderatorEventsListener = new EventsListener();
        var anotherPlayerEventsListener = new EventsListener();

        var roomCode = moderator.createRoom().code();
        player.setRoom(roomCode);

        var destination = "/user/queue/responses/rooms.join";
        anotherPlayer.session.subscribe(destination, anotherPlayerEventsListener);
        moderator.session.subscribe(destination, moderatorEventsListener);

        var responseForPlayer = player.joinRoom();

        assertThat(responseForPlayer, is(notNullValue()));
        assertThat(anotherPlayerEventsListener.getEvents().isEmpty(), is(true));
        assertThat(moderatorEventsListener.getEvents().isEmpty(), is(true));
    }


}
