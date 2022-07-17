package com.example.app;

import com.example.app.room.OngoingQuizStatus;
import com.example.app.room.events.*;
import lombok.Builder;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SimpleGameCaseTest extends AbstractSessionTest {

    private static final int DEFAULT_TIMEOUT_IN_MS = 100;

    @Test
    void simpleGame() throws Exception {
        // given
        var moderator = new PlayerJoystick(DEFAULT_USER_NAME, session);
        var player = new PlayerJoystick("SomePlayer", createSession("SomePlayer"));

        // step 1 - moderator creates room
        var createRoomResponse = moderator.createRoom();
        var roomCode = createRoomResponse.code();
        assertThat(roomCode, is(notNullValue()));

        moderator.setRoom(roomCode);
        player.setRoom(roomCode);

        moderator.subscribeToRoomEvents();

        // step 2 - player joins room
        var joinRoomResponse = player.joinRoom();
        assertThat(joinRoomResponse.ok(), is(true));

        var moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        assertThat(moderatorRoomEvent, is(notNullValue()));
        assertThat(moderatorRoomEvent.room(), is(notNullValue()));
        assertThat(moderatorRoomEvent.room().players(), containsInAnyOrder(moderator.playerName, player.playerName));
        assertThat(moderatorRoomEvent.room().ongoingQuiz(), is(nullValue()));

        player.subscribeToRoomEvents();
        assertThat(player.events.isEmpty(), is(true));

        // step 3 - assign quiz
        var assignQuizResponse = moderator.assignQuiz();
        assertThat(assignQuizResponse.ok(), is(true));

        moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        var playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        assertThat(moderatorRoomEvent, is(notNullValue()));
        assertThat(moderatorRoomEvent.room(), is(notNullValue()));
        assertThat(moderatorRoomEvent.room().players(), containsInAnyOrder(moderator.playerName, player.playerName));
        var ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz, is(notNullValue()));
        assertThat(ongoingQuiz.currentQuestion(), is(0));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.NOT_STARTED));
        assertThat(ongoingQuiz.points(), is(aMapWithSize(2)));
        assertThat(ongoingQuiz.points(), hasEntry(moderator.playerName, 0));
        assertThat(ongoingQuiz.points(), hasEntry(player.playerName, 0));
        assertThat(ongoingQuiz.quiz(), is(notNullValue()));

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        // step 4 - moderator starts quiz
        var startQuizResponse = moderator.startQuiz();
        assertThat(startQuizResponse.ok(), is(true));

        moderatorRoomEvent = moderator.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        playerRoomEvent = player.events.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

        assertThat(moderatorRoomEvent, is(playerRoomEvent));

        ongoingQuiz = moderatorRoomEvent.room().ongoingQuiz();
        assertThat(ongoingQuiz, is(notNullValue()));
        assertThat(ongoingQuiz.currentQuestion(), is(1));
        assertThat(ongoingQuiz.status(), is(OngoingQuizStatus.QUESTION_IN_PROGRESS));

    }

    @Test
    void doesNotJoinNotExistingRoom() {
        var moderator = new PlayerJoystick(DEFAULT_USER_NAME, session);
        moderator.setRoom("012345");

        JoinRoomResponse response = moderator.joinRoom();

        assertThat(response.ok(), is(false));
    }

    @Test
    void doesNotSendResponsesToAnotherSession() {
        var moderator = new PlayerJoystick(DEFAULT_USER_NAME, session);
        var player = new PlayerJoystick("SomePlayer", createSession("SomePlayer"));

        var roomCode = moderator.createRoom().code();
        moderator.setRoom(roomCode);

        var anotherSessionJoinRoomResponseFuture =
                expectResponse(player.session, "/user/queue/responses/rooms.join", JoinRoomResponse.class);

        JoinRoomResponse responseForDefaultSession = moderator.joinRoom();
        JoinRoomResponse responseForAnotherSession = getImmediatelyAndSafely(anotherSessionJoinRoomResponseFuture);

        assertThat(responseForDefaultSession, is(notNullValue()));
        assertThat(responseForAnotherSession, is(nullValue()));
    }

    private class PlayerJoystick {

        private final String playerName;
        private final StompSession session;

        @Setter
        private String room;

        private BlockingQueue<RoomEvent> events;

        public PlayerJoystick(String playerName, StompSession session) {
            this.playerName = playerName;
            this.session = session;
        }

        private CreateRoomResponse createRoom() {
            var params = RequestReplyOperationParams.<CreateRoomRequest, CreateRoomResponse>builder()
                    .session(session)
                    .operation("rooms.create")
                    .request(new CreateRoomRequest())
                    .responseClass(CreateRoomResponse.class)
                    .build();
            return requestAndWaitForReply(params);
        }

        private JoinRoomResponse joinRoom() {
            var params = RequestReplyOperationParams.<JoinRoomRequest, JoinRoomResponse>builder()
                    .session(session)
                    .operation("rooms.join")
                    .request(new JoinRoomRequest(room))
                    .responseClass(JoinRoomResponse.class)
                    .build();
            return requestAndWaitForReply(params);
        }

        private void subscribeToRoomEvents() {
            events = subscribe(session, "/topic/rooms." + room, RoomEvent.class);
        }

        private AssignQuizResponse assignQuiz() {
            var params = RequestReplyOperationParams.<AssignQuizRequest, AssignQuizResponse>builder()
                    .session(session)
                    .operation("rooms.assign-quiz")
                    .request(new AssignQuizRequest(room, 1))
                    .responseClass(AssignQuizResponse.class)
                    .build();
            return requestAndWaitForReply(params);
        }

        public StartQuizResponse startQuiz() {
            var params = RequestReplyOperationParams.<StartQuizRequest, StartQuizResponse>builder()
                    .session(session)
                    .operation("rooms.start-quiz")
                    .request(new StartQuizRequest(room))
                    .responseClass(StartQuizResponse.class)
                    .build();
            return requestAndWaitForReply(params);
        }
    }

    @Builder
    private static final class RequestReplyOperationParams<REQ, RES> {
        private final StompSession session;
        private final String operation;
        private final REQ request;
        private final Class<RES> responseClass;
        @Builder.Default
        private final boolean noResponseIsOkay = false;
        @Builder.Default
        private final int timeoutInMs = DEFAULT_TIMEOUT_IN_MS;

    }

    private <REQ, RES> RES requestAndWaitForReply(RequestReplyOperationParams<REQ, RES> params) {
        var responseFuture =
                expectResponse(params.session, "/user/queue/" + params.operation, params.responseClass);
        params.session.send("/app/" + params.operation, params.request);
        try {
            Thread.sleep(params.timeoutInMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        RES response = getImmediatelyAndSafely(responseFuture);
        if (!params.noResponseIsOkay && response == null) {
            throw new IllegalStateException("Operation '" + params.operation + "' failed. Expected to get response within " + params.timeoutInMs + " ms.");
        }
        return response;
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
