package com.example.secureapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private SecretKey signingKey;
    private RuntimeException keyInitializationFailure;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${security.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        try {
            this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (RuntimeException exception) {
            this.keyInitializationFailure = exception;
        }
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @PostConstruct
    void validateKey() {
        if (keyInitializationFailure != null) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes", keyInitializationFailure);
        }
    }

    public String createAccessToken(UserDetails user) {
        return createToken(user, "access", accessExpirationMs);
    }

    public String createRefreshToken(UserDetails user) {
        return createToken(user, "refresh", refreshExpirationMs);
    }

    private String createToken(UserDetails user, String type, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails user) {
        return isTokenValid(token, user, "access");
    }

    public boolean isRefreshTokenValid(String token, UserDetails user) {
        return isTokenValid(token, user, "refresh");
    }

    private boolean isTokenValid(String token, UserDetails user, String expectedType) {
        Claims claims = parseClaims(token);
        return expectedType.equals(claims.get("type", String.class))
                && user.getUsername().equals(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}