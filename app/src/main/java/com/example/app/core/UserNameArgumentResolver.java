package com.example.app.core;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

public class UserNameArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String USER_NAME_ATTRIBUTE_NAME = "QUIZ-APP-USER-NAME";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UserName.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);

        // TODO: be less optimistic
        // TODO: maybe set later as principal? accessor.setUser
        return new UserName((String) accessor.getSessionAttributes().get(USER_NAME_ATTRIBUTE_NAME));
    }
}
