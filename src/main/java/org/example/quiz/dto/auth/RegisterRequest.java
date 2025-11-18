package org.example.quiz.dto.auth;

public record RegisterRequest(
        String username,
        String password,
        String displayName
) {}



