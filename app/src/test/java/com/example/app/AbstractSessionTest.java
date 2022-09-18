package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Import(WebSocketTestClientConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AbstractSessionTest {

    protected static final int DEFAULT_TIMEOUT_IN_MS = 100;

    @Autowired
    private WebSocketStompClient client;
    @Autowired
    protected ObjectMapper objectMapper;
    @LocalServerPort
    private int port;

    protected StompSession createSession(String userName) {
        try {
            String encodedName = Base64.getEncoder().encodeToString(userName.getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Player " + encodedName);
            return client
                    .connect(String.format("ws://localhost:%d/api", port),
                            new WebSocketHttpHeaders(headers),
                            new StompHeaders(),
                            new StompSessionHandlerAdapter() {
                            })
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Could not get session", e);
        }
    }

    protected <REQ, RES> RequestReplyOperation.RequestReplyOperationBuilder<REQ, RES> newRequestReplyOperation() {
        return RequestReplyOperation.<REQ, RES>builder().objectMapper(objectMapper);
    }

}
