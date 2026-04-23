package com.project.guardrails.dtos;

public record CreateLikeRequest(
        Long authorId,
        String authorType
) {
}
