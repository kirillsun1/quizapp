package com.example.app.playscenarios;

import com.example.app.AbstractSessionTest;
import com.example.app.RequestReplyOperation;
import com.example.app.room.events.AssignQuizRequest;
import com.example.app.room.events.AssignQuizResponse;
import com.example.app.room.events.CreateRoomRequest;
import com.example.app.room.events.CreateRoomResponse;
import com.example.app.room.events.MoveOnRequest;
import com.example.app.room.events.MoveOnResponse;
import com.example.app.room.events.JoinRoomRequest;
import com.example.app.room.events.JoinRoomResponse;
import com.example.app.room.events.RoomEvent;
import com.example.app.room.events.StartQuizRequest;
import com.example.app.room.events.StartQuizResponse;
import com.example.app.room.events.VoteRequest;
import com.example.app.room.events.VoteResponse;
import lombok.Setter;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.BlockingQueue;

public class AbstractPlayScenarioTest extends AbstractSessionTest {

    private abstract class Joystick {

        final String playerName;
        final StompSession session;

        @Setter
        protected String room;

        BlockingQueue<RoomEvent> events;

        public Joystick(String playerName, StompSession session) {
            this.playerName = playerName;
            this.session = session;
        }

        public void subscribeToRoomEvents() {
            events = subscribe(session, "/topic/rooms." + room, RoomEvent.class);
        }

    }

    protected class ModeratorJoystick extends Joystick {

        protected ModeratorJoystick(String playerName, StompSession session) {
            super(playerName, session);
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

        public AssignQuizResponse assignQuiz() {
            return RequestReplyOperation.<AssignQuizRequest, AssignQuizResponse>builder()
                    .responseMessagesListener(createListener(AssignQuizResponse.class))
                    .session(session)
                    .operation("rooms.quiz.assign")
                    .request(new AssignQuizRequest(room, 1))
                    .build()
                    .execute();
        }

        public StartQuizResponse startQuiz() {
            return RequestReplyOperation.<StartQuizRequest, StartQuizResponse>builder()
                    .responseMessagesListener(createListener(StartQuizResponse.class))
                    .session(session)
                    .operation("rooms.quiz.start")
                    .request(new StartQuizRequest(room))
                    .build()
                    .execute();
        }

        public MoveOnResponse moveOn() {
            return RequestReplyOperation.<MoveOnRequest, MoveOnResponse>builder()
                    .responseMessagesListener(createListener(MoveOnResponse.class))
                    .session(session)
                    .operation("rooms.quiz.move-on")
                    .request(new MoveOnRequest(room))
                    .build()
                    .execute();
        }
    }

    protected class PlayerJoystick extends Joystick {

        protected PlayerJoystick(String playerName, StompSession session) {
            super(playerName, session);
        }

        public JoinRoomResponse joinRoom() {
            return RequestReplyOperation.<JoinRoomRequest, JoinRoomResponse>builder()
                    .responseMessagesListener(createListener(JoinRoomResponse.class))
                    .session(session)
                    .operation("rooms.join")
                    .request(new JoinRoomRequest(room))
                    .build()
                    .execute();
        }

        public VoteResponse vote(int choice) {
            return RequestReplyOperation.<VoteRequest, VoteResponse>builder()
                    .responseMessagesListener(createListener(VoteResponse.class))
                    .session(session)
                    .operation("rooms.quiz.vote")
                    .request(new VoteRequest(room, choice))
                    .build()
                    .execute();
        }
    }

}
