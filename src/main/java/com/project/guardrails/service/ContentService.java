package com.project.guardrails.service;

import com.project.guardrails.dtos.CreateCommentRequest;
import com.project.guardrails.dtos.CreatePostRequest;
import com.project.guardrails.enums.AuthorType;
import com.project.guardrails.model.Comment;
import com.project.guardrails.model.Post;
import com.project.guardrails.repository.CommentRepository;
import com.project.guardrails.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public Post createPost(CreatePostRequest postRequest) {
        Post post = Post.builder()
                .authorId(postRequest.authorId())
                .authorType(AuthorType.valueOf(postRequest.authorType().toUpperCase()))
                .content(postRequest.content())
                .build();
        return postRepository.save(post);
    }

    public Comment addComment(Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .post(post)
                .authorId(request.authorId())
                .authorType(AuthorType.valueOf(request.AuthorType().toUpperCase()))
                .content(request.content())
                .build();
        comment.setDepthLevel(1);

        return commentRepository.save(comment);
    }

    @Transactional
    public void likePost(Long postId) {

        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        System.out.println("Post " + postId + " liked! (Redis Virality integration pending)");
    }
}
