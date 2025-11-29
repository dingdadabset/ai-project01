package com.aiproject.module.post.controller;

import com.aiproject.module.post.model.PostRequest;
import com.aiproject.module.post.model.PostResponse;
import com.aiproject.module.post.service.PostService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<IPage<PostResponse>> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/posts - page: {}, size: {}", page, size);
        IPage<PostResponse> posts = postService.listPosts(page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * List published posts
     */
    @GetMapping("/published")
    public ResponseEntity<IPage<PostResponse>> listPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/posts/published");
        IPage<PostResponse> posts = postService.listPublishedPosts(page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * List posts by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<IPage<PostResponse>> listPostsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/posts/category/{}", categoryId);
        IPage<PostResponse> posts = postService.listPostsByCategory(categoryId, page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * List posts by tag
     */
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<IPage<PostResponse>> listPostsByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/posts/tag/{}", tagId);
        IPage<PostResponse> posts = postService.listPostsByTag(tagId, page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * Search posts by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<IPage<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/posts/search - keyword: {}", keyword);
        IPage<PostResponse> posts = postService.searchPosts(keyword, page, size);
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
