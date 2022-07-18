package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Import(WebSocketTestClientConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AbstractSessionTest {

    protected static final int DEFAULT_TIMEOUT_IN_MS = 100;

    @Autowired
    private WebSocketStompClient client;
    @Autowired
    private ObjectMapper objectMapper;
    @LocalServerPort
    private int port;

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

    protected <T> Future<T> expectReply(StompSession session, String destination, Class<T> clazz) {
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
        var handler = createListener(clazz);
        session.subscribe(destination, handler);
        return handler.getQueue();
    }

    protected <T> ResponseMessagesListener<T> createListener(Class<T> clazz) {
        return new ResponseMessagesListener<>(bytes -> readFromBytes(bytes, clazz));
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
