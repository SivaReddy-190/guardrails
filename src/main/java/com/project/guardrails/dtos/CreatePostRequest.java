package com.project.guardrails.dtos;

public record CreatePostRequest(
        Long authorId,
        String authorType,
        String content

) {}
