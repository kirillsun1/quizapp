package com.example.app;

import com.example.app.ResponseMessagesListener;
import lombok.Builder;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.TimeUnit;

@Builder
public class RequestReplyOperation<REQ, RES> {

    private static final int DEFAULT_TIMEOUT_IN_MS = 100;

    private final ResponseMessagesListener<RES> responseMessagesListener;
    private final StompSession session;
    private final String operation;
    private final REQ request;
    @Builder.Default
    private final boolean failIfNoResponse = true;
    @Builder.Default
    private final int timeoutInMs = DEFAULT_TIMEOUT_IN_MS;

    public RES execute() {
        session.subscribe("/user/queue/" + operation, responseMessagesListener);
        session.send("/app/" + operation, request);

        try {
            var response = responseMessagesListener.getQueue().poll(timeoutInMs, TimeUnit.MILLISECONDS);
            if (response == null && failIfNoResponse) {
                throw new IllegalStateException("Operation '" + operation + "' failed. Expected to get response within " + timeoutInMs + " ms.");
            }
            return response;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
