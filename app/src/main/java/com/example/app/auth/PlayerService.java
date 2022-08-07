package com.example.app.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class PlayerService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        return new Player(username);
    }
}
