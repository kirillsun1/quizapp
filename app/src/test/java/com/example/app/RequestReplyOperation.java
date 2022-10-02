package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import org.springframework.messaging.simp.stomp.StompSession;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Builder
public class RequestReplyOperation<REQ, RES> {

    private static final int DEFAULT_TIMEOUT_IN_MS = 5000;

    private final ObjectMapper objectMapper;
    private final StompSession session;
    private final String operation;
    private final EventsListener eventsListener;
    private final REQ request;
    private final Class<RES> responseClass;
    @Builder.Default
    private final boolean failIfNoResponse = true;
    @Builder.Default
    private final int timeoutInMs = DEFAULT_TIMEOUT_IN_MS;

    public RES execute() {
        StompSession.Subscription subscription = session.subscribe("/user/queue/" + operation, eventsListener);
        session.send("/app/" + operation, request);

        try {
            var response = eventsListener.getEvents().poll(timeoutInMs, TimeUnit.MILLISECONDS);
            if (response == null) {
                if (failIfNoResponse) {
                    throw new IllegalStateException("Operation '" + operation + "' failed. Expected to get response within " + timeoutInMs + " ms.");
                }
                return null;
            }
            var jsonNode = objectMapper.readTree(response);
            var error = jsonNode.get("error");
            if (error == null || !error.isBoolean()) {
                throw new IllegalStateException("Unexpected response: " + new String(response));
            }
            if (error.asBoolean()) {
                var errorCode = jsonNode.get("errorCode").asText();
                throw new RuntimeException("Operation '" + operation + "' failed with code " + errorCode + ".");
            }
            if (responseClass == null) {
                return null;
            }
            return objectMapper.treeToValue(jsonNode, responseClass);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            subscription.unsubscribe();
        }
    }

}
