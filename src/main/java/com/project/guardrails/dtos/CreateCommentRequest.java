package com.project.guardrails.dtos;

public record CreateCommentRequest(
        Long authorId,
        String AuthorType,
        String content

) {
}
