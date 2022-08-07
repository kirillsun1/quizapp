package com.example.app.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class PlayerService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        // TODO: check if player is already logged in. What then?
        return new Player(username);
    }
}
