package com.project.guardrails.service;

import com.project.guardrails.dtos.CreateCommentRequest;
import com.project.guardrails.dtos.CreatePostRequest;
import com.project.guardrails.enums.AuthorType;
import com.project.guardrails.exception.RateLimitExceedException;
import com.project.guardrails.model.Comment;
import com.project.guardrails.model.Post;
import com.project.guardrails.repository.BotRepository;
import com.project.guardrails.repository.CommentRepository;
import com.project.guardrails.repository.PostRepository;
import com.project.guardrails.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final RedisGuardrailService redisGuardrailService;

//    private void validateUser(Long authorId, AuthorType type) {
//        if (type == AuthorType.USER) {
//            if (!userRepository.existsById(authorId)) {
//                throw new RuntimeException("User not found");
//            }
//        } else if (type == AuthorType.BOT) {
//            if (!botRepository.existsById(authorId)) {
//                throw new RuntimeException("Bot not found");
//            }
//        }
//
//    }

    @Transactional
    public Post createPost(CreatePostRequest postRequest) {
//        AuthorType type = AuthorType.valueOf(postRequest.authorType().toUpperCase());
//        validateUser(postRequest.authorId(), type);

        Post post = Post.builder()
                .authorId(postRequest.authorId())
                .authorType(AuthorType.valueOf(postRequest.authorType().toUpperCase()))
                .content(postRequest.content())
                .build();
        return postRepository.save(post);
    }

    public Comment addComment(Long postId, CreateCommentRequest commentRequest, Long parentCommentId) {

//        AuthorType type = AuthorType.valueOf(commentRequest.authorType().toUpperCase());
//        validateUser(commentRequest.authorId(), type);

        AuthorType type = AuthorType.valueOf(commentRequest.authorType().toUpperCase());
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean isBot = type == AuthorType.BOT;
        int depth = 1;
        System.out.println(parentCommentId);
        if(parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            depth = parent.getDepthLevel() + 1;

            if (depth > 20) {
                throw new RateLimitExceedException("A comment thread cannot go deeper than 20 levels");
            }

            if (isBot) {

                if (!redisGuardrailService.allowBotCommentsOnPost(postId)) {
                    throw new RateLimitExceedException("This post has reached the maximum number of bot comments allowed");
                }

                if (post.getAuthorType() == AuthorType.USER) {
                    if (!redisGuardrailService.checkAndSetBotCooldown(commentRequest.authorId(), post.getAuthorId())) {
                        throw new RateLimitExceedException("This bot is on cooldown for commenting on posts by this user");
                    }
                }
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .authorId(commentRequest.authorId())
                .authorType(AuthorType.valueOf(commentRequest.authorType().toUpperCase()))
                .content(commentRequest.content())
                .build();
        comment.setDepthLevel(depth);

        Comment savedComment = commentRepository.save(comment);

        int point = isBot?1:50;
        redisGuardrailService.incrementViralityScore(postId, point);
        return savedComment;
    }

    @Transactional
    public void likePost(Long postId, Long authorId, String authorType) {

//        AuthorType type = AuthorType.valueOf(authorType.toUpperCase());
//        validateUser(authorId, type);



        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

//        if(type == AuthorType.USER) {
//            redisGuardrailService.incrementViralityScore(postId, 10);
//        }
    }
}
