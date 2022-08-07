package com.example.app.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

public class PlayerTokenExtractor implements AuthenticationConverter {

    private static final String AUTH_HEADER_VALUE_PREFIX = "Player ";

    private final Base64.Decoder decoder = Base64.getDecoder();

    @Override
    public Authentication convert(HttpServletRequest request) {
        String authToken = findAuthenticationToken(request);
        if (authToken == null) {
            return null;
        }
        try {
            String playerName = new String(decoder.decode(authToken));
            return new PreAuthenticatedAuthenticationToken(playerName, authToken);
        } catch (IllegalArgumentException ex) {
            throw new BadCredentialsException("Authentication failed. Player name is not encoded properly", ex);
        }
    }

    private String findAuthenticationToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            if (authHeader.startsWith(AUTH_HEADER_VALUE_PREFIX)) {
                return authHeader.substring(AUTH_HEADER_VALUE_PREFIX.length());
            }
            return null;
        }
        // WORKAROUND: Browsers don't support adding headers to websocket handshare request
        return request.getParameter("playerToken");
    }

}
