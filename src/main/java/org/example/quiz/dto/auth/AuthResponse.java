package org.example.quiz.dto.auth;

public record AuthResponse(
        String token,
        String username,
        String displayName,
        String role
) {}