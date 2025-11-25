package com.aiproject.module.comment.repository;

import com.aiproject.module.comment.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Comment Repository
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    Page<Comment> findByPostId(Long postId, Pageable pageable);
    
    Page<Comment> findByStatus(Comment.CommentStatus status, Pageable pageable);
    
    long countByPostId(Long postId);
}
