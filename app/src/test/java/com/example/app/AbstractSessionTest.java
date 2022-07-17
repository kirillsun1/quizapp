package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.*;

@Import(WebSocketTestClientConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AbstractSessionTest {

    private static final int DEFAULT_EVENTS_QUEUE_CAPACITY = 100;
    protected static final String DEFAULT_USER_NAME = "QUIZ-APP-TEST";

    @Autowired
    private WebSocketStompClient client;
    @Autowired
    private ObjectMapper objectMapper;
    @LocalServerPort
    private int port;

    protected StompSession session;

    @BeforeEach
    void setUp() {
        session = createSession(DEFAULT_USER_NAME);
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            session.disconnect();
        }
    }

    protected StompSession createSession(String userName) {
        try {
            var headers = new HttpHeaders();
            headers.set("X-Username", userName);
            return client
                    .connect(String.format("ws://localhost:%d/ws-endpoint", port),
                            new WebSocketHttpHeaders(headers),
                            new StompSessionHandlerAdapter() {
                            })
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Could not get session", e);
        }
    }

    protected <T> Future<T> expectResponse(StompSession session, String destination, Class<T> clazz) {
        var queue = subscribe(session, destination, clazz);
        return new CompletableFuture<T>().completeAsync(() -> {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected <T> BlockingQueue<T> subscribe(StompSession session, String destination, Class<T> clazz) {
        var queue = new ArrayBlockingQueue<T>(DEFAULT_EVENTS_QUEUE_CAPACITY);
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add(readFromBytes((byte[]) payload, clazz));
            }
        });
        return queue;
    }

    protected <T> T readFromBytes(byte[] bytes, Class<T> clazz) {
        try {
            if (bytes == null) {
                return null;
            }
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}