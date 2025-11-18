package org.example.quiz.dto;

public record CategoryDto(
        Long id,
        String name,
        Integer questionCount
) {}