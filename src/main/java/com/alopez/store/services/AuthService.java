package com.alopez.store.services;

import com.alopez.store.entities.User;
import com.alopez.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;


    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userID = (Long) authentication.getPrincipal();

        return userRepository.findById(userID).orElse(null);
    }
}
