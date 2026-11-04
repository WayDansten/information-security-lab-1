package com.example.secureapi.auth;

public record TokenResponse(String accessToken, String refreshToken, String tokenType) {
}