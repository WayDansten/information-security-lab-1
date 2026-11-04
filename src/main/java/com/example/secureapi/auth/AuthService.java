package com.example.secureapi.auth;

import com.example.secureapi.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        return tokensFor(user);
    }

    public TokenResponse refresh(RefreshRequest request) {
        String username = jwtService.extractUsername(request.refreshToken());
        UserDetails user = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isRefreshTokenValid(request.refreshToken(), user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        return tokensFor(user);
    }

    private TokenResponse tokensFor(UserDetails user) {
        return new TokenResponse(
                jwtService.createAccessToken(user),
                jwtService.createRefreshToken(user),
                "Bearer");
    }
}