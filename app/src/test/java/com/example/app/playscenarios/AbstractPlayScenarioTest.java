package com.example.app.playscenarios;

import com.example.app.AbstractSessionTest;
import com.example.app.EventsListener;
import com.example.app.ongoingquiz.events.MoveOnRequest;
import com.example.app.ongoingquiz.events.VoteRequest;
import com.example.app.room.Room;
import com.example.app.room.events.AssignQuizRequest;
import com.example.app.room.events.CreateRoomResponse;
import com.example.app.room.events.JoinRoomResponse;
import com.example.app.room.events.RoomEvent;
import lombok.Setter;
import org.springframework.messaging.simp.stomp.StompSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AbstractPlayScenarioTest extends AbstractSessionTest {

    private abstract class Joystick {

        final String playerName;
        StompSession session;

        @Setter
        protected String room;

        BlockingQueue<byte[]> events;

        public Joystick(String playerName) {
            this.playerName = playerName;
        }

        public void createSession() {
            session = AbstractPlayScenarioTest.this.createSession(playerName);
        }

        public void subscribeToRoomEvents() {
            var handler = new EventsListener();
            session.subscribe("/topic/rooms." + room, handler);
            events = handler.getEvents();
        }

        RoomEvent nextRoomEvent() {
            try {
                byte[] response = events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
                return response != null ? objectMapper.readValue(response, RoomEvent.class) : null;
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    protected class ModeratorJoystick extends Joystick {

        protected ModeratorJoystick(String playerName) {
            super("Moderator-" + playerName);
        }

        public Room createRoom() {
            return AbstractPlayScenarioTest.this.<Object, CreateRoomResponse>newRequestReplyOperation()
                    .eventsListener(new EventsListener())
                    .session(session)
                    .operation("rooms.create")
                    .request(Map.of())
                    .responseClass(CreateRoomResponse.class)
                    .build()
                    .execute()
                    .getRoom();
        }

        public void assignQuiz(int id) {
            newRequestReplyOperation()
                    .eventsListener(new EventsListener())
                    .session(session)
                    .operation("rooms/" + room + "/quiz.assign")
                    .request(new AssignQuizRequest(id))
                    .build()
                    .execute();
        }

        public void moveOn() {
            newRequestReplyOperation()
                    .eventsListener(new EventsListener())
                    .session(session)
                    .operation("rooms/" + room + "/quiz.move-on")
                    .request(new MoveOnRequest())
                    .build()
                    .execute();
        }
    }

    protected class PlayerJoystick extends Joystick {

        protected PlayerJoystick(String playerName) {
            super("Player-" + playerName);
        }

        public Room joinRoom() {
            return AbstractPlayScenarioTest.this.<Object, JoinRoomResponse>newRequestReplyOperation()
                    .eventsListener(new EventsListener())
                    .session(session)
                    .operation("rooms/" + room + ".join")
                    .request(Map.of())
                    .responseClass(JoinRoomResponse.class)
                    .build()
                    .execute()
                    .getRoom();
        }

        public void vote(int choice) {
            newRequestReplyOperation()
                    .eventsListener(new EventsListener())
                    .session(session)
                    .operation("rooms/" + room + "/quiz.vote")
                    .request(new VoteRequest(choice))
                    .build()
                    .execute();
        }
    }

}
