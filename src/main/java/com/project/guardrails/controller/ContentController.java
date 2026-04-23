package com.project.guardrails.controller;

import com.project.guardrails.dtos.CreateCommentRequest;
import com.project.guardrails.dtos.CreateLikeRequest;
import com.project.guardrails.dtos.CreatePostRequest;
import com.project.guardrails.model.Comment;
import com.project.guardrails.model.Post;
import com.project.guardrails.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;


    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest request) {
        Post createdPost = contentService.createPost(request);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }


    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long postId,
            @RequestParam(required = false) Long parentCommentId,
            @RequestBody CreateCommentRequest request) {

        Comment createdComment = contentService.addComment(postId, request, parentCommentId);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long postId, @RequestBody CreateLikeRequest request) {
        contentService.likePost(postId, request.authorId(),request.authorType() );
        return ResponseEntity.ok("Post liked successfully");
    }
}