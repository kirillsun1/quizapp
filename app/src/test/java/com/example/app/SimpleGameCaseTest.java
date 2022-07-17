package com.example.app;

import com.example.app.room.events.*;
import lombok.Builder;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SimpleGameCaseTest extends AbstractSessionTest {

    private static final int DEFAULT_TIMEOUT_IN_MS = 500;

    @Test
    void simpleTestCase() throws Exception {
        // given
        var moderatorSession = session;
        var playerName = "SomePlayer";
        var playerSession = createSession(playerName);

        // step 1 - moderator creates room
        var createRoomResponse = createRoom(moderatorSession);
        var roomCode = createRoomResponse.code();
        assertThat(roomCode, is(notNullValue()));

        var moderatorRoomEvents = subscribeToRoomEvents(moderatorSession, roomCode);

        // step 2 - player joins room
        var joinRoomResponse = joinRoom(playerSession, roomCode);
        assertThat(joinRoomResponse.ok(), is(true));

        var roomEvent = moderatorRoomEvents.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        assertThat(roomEvent, is(notNullValue()));
        assertThat(roomEvent.room(), is(notNullValue()));
        assertThat(roomEvent.room().players(), containsInAnyOrder(DEFAULT_USER_NAME, playerName));
        assertThat(roomEvent.room().ongoingQuiz(), is(nullValue()));

        var playerRoomEvents = subscribeToRoomEvents(playerSession, roomCode);
        assertThat(playerRoomEvents.isEmpty(), is(true));

        // step 3
        var assignQuizResponse = assignQuiz(moderatorSession, roomCode);
        assertThat(assignQuizResponse.ok(), is(true));

        roomEvent = moderatorRoomEvents.poll(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        assertThat(roomEvent, is(notNullValue()));
        assertThat(roomEvent.room(), is(notNullValue()));
        assertThat(roomEvent.room().players(), containsInAnyOrder(DEFAULT_USER_NAME, playerName));
        assertThat(roomEvent.room().ongoingQuiz(), is(notNullValue()));
    }

    @Test
    void doesNotJoinNotExistingRoom() {
        JoinRoomResponse response = joinRoom(session, "012345");

        assertThat(response.ok(), is(false));
    }

    @Test
    void doesNotSendResponsesToAnotherSession() {
        var roomCode = createRoom(session).code();
        var anotherSession = createSession("Another Player");
        var anotherSessionJoinRoomResponseFuture =
                expectResponse(anotherSession, "/user/queue/responses/rooms.join", JoinRoomResponse.class);

        JoinRoomResponse responseForDefaultSession = joinRoom(session, roomCode);
        JoinRoomResponse responseForAnotherSession = getImmediatelyAndSafely(anotherSessionJoinRoomResponseFuture);

        assertThat(responseForDefaultSession, is(notNullValue()));
        assertThat(responseForAnotherSession, is(nullValue()));
    }

    private CreateRoomResponse createRoom(StompSession session) {
        var params = RequestReplyOperationParams.<CreateRoomRequest, CreateRoomResponse>builder()
                .session(session)
                .operation("rooms.create")
                .request(new CreateRoomRequest())
                .responseClass(CreateRoomResponse.class)
                .build();
        return requestAndWaitForReply(params);
    }

    private JoinRoomResponse joinRoom(StompSession session, String code) {
        var params = RequestReplyOperationParams.<JoinRoomRequest, JoinRoomResponse>builder()
                .session(session)
                .operation("rooms.join")
                .request(new JoinRoomRequest(code))
                .responseClass(JoinRoomResponse.class)
                .build();
        return requestAndWaitForReply(params);
    }

    private AssignQuizResponse assignQuiz(StompSession session, String roomCode) {
        var params = RequestReplyOperationParams.<AssignQuizRequest, AssignQuizResponse>builder()
                .session(session)
                .operation("rooms.assign-quiz")
                .request(new AssignQuizRequest(roomCode, 1))
                .responseClass(AssignQuizResponse.class)
                .build();
        return requestAndWaitForReply(params);
    }

    private BlockingQueue<RoomEvent> subscribeToRoomEvents(StompSession session, String roomCode) {
        return subscribe(session, "/topic/rooms." + roomCode, RoomEvent.class);
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

}
