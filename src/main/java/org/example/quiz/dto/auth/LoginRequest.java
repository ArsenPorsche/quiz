package org.example.quiz.dto.auth;

public record LoginRequest(
        String username,
        String password
) {}