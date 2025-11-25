package com.aiproject.module.post.repository;

import com.aiproject.module.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Post Repository
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    Optional<Post> findBySlug(String slug);
    
    Page<Post> findByStatus(Post.PostStatus status, Pageable pageable);
    
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
    
    Page<Post> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    
    boolean existsBySlug(String slug);
}
