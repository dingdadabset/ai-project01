package com.aiproject.module.comment.service;

import com.aiproject.module.comment.model.Comment;
import com.aiproject.module.comment.repository.CommentRepository;
import com.aiproject.module.post.model.Post;
import com.aiproject.module.post.repository.PostRepository;
import com.aiproject.module.user.model.User;
import com.aiproject.module.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comment Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment createComment(Long postId, Long userId, String content, String guestName, String guestEmail) {
        log.info("Creating comment on post: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        
        Comment.CommentBuilder builder = Comment.builder()
                .post(post)
                .content(content)
                .status(Comment.CommentStatus.PENDING);
        
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            builder.user(user);
        } else {
            builder.guestName(guestName).guestEmail(guestEmail);
        }
        
        Comment comment = commentRepository.save(builder.build());
        
        // Update post comment count
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        
        return comment;
    }

    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }

    @Transactional
    public Comment approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        comment.setStatus(Comment.CommentStatus.APPROVED);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        commentRepository.delete(comment);
        
        // Update post comment count
        Post post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);
    }
}
