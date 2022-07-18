package com.example.app.playscenarios;

import com.example.app.AbstractSessionTest;
import com.example.app.RequestReplyOperation;
import com.example.app.ongoingquiz.events.MoveOnRequest;
import com.example.app.ongoingquiz.events.MoveOnResponse;
import com.example.app.ongoingquiz.events.VoteRequest;
import com.example.app.ongoingquiz.events.VoteResponse;
import com.example.app.room.events.AssignQuizRequest;
import com.example.app.room.events.AssignQuizResponse;
import com.example.app.room.events.CreateRoomRequest;
import com.example.app.room.events.CreateRoomResponse;
import com.example.app.room.events.JoinRoomRequest;
import com.example.app.room.events.JoinRoomResponse;
import com.example.app.room.events.RoomEvent;
import lombok.Setter;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.BlockingQueue;

public class AbstractPlayScenarioTest extends AbstractSessionTest {

    private abstract class Joystick {

        final String playerName;
        StompSession session;

        @Setter
        protected String room;

        BlockingQueue<RoomEvent> events;

        public Joystick(String playerName) {
            this.playerName = playerName;
        }

        public void createSession() {
            session = AbstractPlayScenarioTest.this.createSession(playerName);
        }

        public void subscribeToRoomEvents() {
            events = subscribe(session, "/topic/rooms." + room, RoomEvent.class);
        }

    }

    protected class ModeratorJoystick extends Joystick {

        protected ModeratorJoystick(String playerName) {
            super(playerName);
        }

        public CreateRoomResponse createRoom() {
            return RequestReplyOperation.<CreateRoomRequest, CreateRoomResponse>builder()
                    .responseMessagesListener(createListener(CreateRoomResponse.class))
                    .session(session)
                    .operation("rooms.create")
                    .request(new CreateRoomRequest())
                    .build()
                    .execute();
        }

        public AssignQuizResponse assignQuiz(int id) {
            return RequestReplyOperation.<AssignQuizRequest, AssignQuizResponse>builder()
                    .responseMessagesListener(createListener(AssignQuizResponse.class))
                    .session(session)
                    .operation("rooms/" + room + "/quiz.assign")
                    .request(new AssignQuizRequest(id))
                    .build()
                    .execute();
        }

        public MoveOnResponse moveOn() {
            return RequestReplyOperation.<MoveOnRequest, MoveOnResponse>builder()
                    .responseMessagesListener(createListener(MoveOnResponse.class))
                    .session(session)
                    .operation("rooms/" + room + "/quiz.move-on")
                    .request(new MoveOnRequest())
                    .build()
                    .execute();
        }
    }

    protected class PlayerJoystick extends Joystick {

        protected PlayerJoystick(String playerName) {
            super(playerName);
        }

        public JoinRoomResponse joinRoom() {
            return RequestReplyOperation.<JoinRoomRequest, JoinRoomResponse>builder()
                    .responseMessagesListener(createListener(JoinRoomResponse.class))
                    .session(session)
                    .operation("rooms/" + room + ".join")
                    .request(new JoinRoomRequest())
                    .build()
                    .execute();
        }

        public VoteResponse vote(int choice) {
            return RequestReplyOperation.<VoteRequest, VoteResponse>builder()
                    .responseMessagesListener(createListener(VoteResponse.class))
                    .session(session)
                    .operation("rooms/" + room + "/quiz.vote")
                    .request(new VoteRequest(choice))
                    .build()
                    .execute();
        }
    }

}
