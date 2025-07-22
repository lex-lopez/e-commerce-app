package com.alopez.store.services;

import com.alopez.store.config.JwtConfig;
import com.alopez.store.dtos.UserLoginRequest;
import com.alopez.store.entities.User;
import com.alopez.store.exceptions.UserNotFoundException;
import com.alopez.store.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    public Jwt login(UserLoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(UserNotFoundException::new);

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        createCookieWithRefreshToken(response, refreshToken);
        return accessToken;
    }

    public Jwt refreshToken(String refreshToken) {
        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired()) {
            throw new AccessDeniedException("Invalid refresh token");
        }

        var user = userRepository.findById(jwt.getUserId()).orElseThrow(UserNotFoundException::new);
        return jwtService.generateAccessToken(user);
    }

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userID = (Long) authentication.getPrincipal();

        return userRepository.findById(userID).orElse(null);
    }

    private void createCookieWithRefreshToken(HttpServletResponse response, Jwt refreshToken) {
        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenTTL());
        cookie.setSecure(true);

        response.addCookie(cookie);
    }
}
