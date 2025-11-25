package com.aiproject.module.post.controller;

import com.aiproject.module.post.model.PostRequest;
import com.aiproject.module.post.model.PostResponse;
import com.aiproject.module.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Post Controller
 * REST API endpoints for blog posts
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Create a new post
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest request,
            @RequestParam(defaultValue = "1") Long authorId) {
        log.info("POST /api/posts - Creating new post");
        PostResponse response = postService.createPost(request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a post
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        log.info("PUT /api/posts/{} - Updating post", id);
        PostResponse response = postService.updatePost(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get post by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        log.info("GET /api/posts/{}", id);
        PostResponse response = postService.getPostById(id);
        postService.incrementViewCount(id); // Increment view count
        return ResponseEntity.ok(response);
    }

    /**
     * Get post by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug) {
        log.info("GET /api/posts/slug/{}", slug);
        PostResponse response = postService.getPostBySlug(slug);
        return ResponseEntity.ok(response);
    }

    /**
     * List all posts with pagination
     */
    @GetMapping
    public ResponseEntity<Page<PostResponse>> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("GET /api/posts - page: {}, size: {}", page, size);
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<PostResponse> posts = postService.listPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * List published posts
     */
    @GetMapping("/published")
    public ResponseEntity<Page<PostResponse>> listPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/posts/published");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<PostResponse> posts = postService.listPublishedPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * List posts by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<PostResponse>> listPostsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/posts/category/{}", categoryId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostResponse> posts = postService.listPostsByCategory(categoryId, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Search posts by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/posts/search - keyword: {}", keyword);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostResponse> posts = postService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Delete a post
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("DELETE /api/posts/{}", id);
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
