package com.example.app.core;

import lombok.Getter;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ErrorHandler {

    @SendToUser(broadcast = false)
    @MessageExceptionHandler
    public ErrorResponse handle(Exception e) {
        if (e instanceof BaseGameException) {
            var code = e.getClass().getSimpleName().replace("Exception", "");
            return new ErrorResponse(code, null);
        } else {
            return new ErrorResponse("UNKNOWN", null);
        }
    }

    @Getter
    public static class ErrorResponse extends BaseResponse {
        private final String errorCode;
        private final Object details;

        public ErrorResponse(String errorCode, Object details) {
            super(true);
            this.errorCode = errorCode;
            this.details = details;
        }
    }

}
