package com.example.app.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

@Configuration
@RequiredArgsConstructor
public class AuthenticationFilterConfiguration {

    private final ApplicationEventPublisher eventPublisher;

    @Bean
    AuthenticationFilter authenticationFilter() {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(createAuthenticationManager(), new PlayerTokenExtractor());
        authenticationFilter.setSuccessHandler((request, response, authentication) -> {
            // default handler makes unnecessary redirections which lead to handshake failure
            // do nothing here
        });
        return authenticationFilter;
    }

    private AuthenticationManager createAuthenticationManager() {
        var provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<>(new PlayerService()));
        var authenticationManager = new ProviderManager(provider);
        authenticationManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(eventPublisher));
        return authenticationManager;
    }

}
