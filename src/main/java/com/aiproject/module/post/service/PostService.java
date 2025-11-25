package com.aiproject.module.post.service;

import com.aiproject.module.category.model.Category;
import com.aiproject.module.category.repository.CategoryRepository;
import com.aiproject.module.post.model.Post;
import com.aiproject.module.post.model.PostRequest;
import com.aiproject.module.post.model.PostResponse;
import com.aiproject.module.post.repository.PostRepository;
import com.aiproject.module.tag.model.Tag;
import com.aiproject.module.tag.repository.TagRepository;
import com.aiproject.module.user.model.User;
import com.aiproject.module.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Post Service
 * Handles blog post business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    /**
     * Create a new post
     */
    @Transactional
    public PostResponse createPost(PostRequest request, Long authorId) {
        log.info("Creating new post: {}", request.getTitle());

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .summary(request.getSummary())
                .content(request.getContent())
                .originalContent(request.getOriginalContent())
                .thumbnail(request.getThumbnail())
                .status(request.getStatus() != null ? request.getStatus() : Post.PostStatus.DRAFT)
                .author(author)
                .allowComment(request.getAllowComment() != null ? request.getAllowComment() : true)
                .build();

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            post.setCategory(category);
        }

        // Set tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            post.setTags(tags);
        }

        // Set published date if status is PUBLISHED
        if (post.getStatus() == Post.PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }

        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }

    /**
     * Update an existing post
     */
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        log.info("Updating post: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        post.setTitle(request.getTitle());
        post.setSlug(generateSlug(request.getTitle()));
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setOriginalContent(request.getOriginalContent());
        post.setThumbnail(request.getThumbnail());

        if (request.getStatus() != null) {
            // If status changes to PUBLISHED, set published date
            if (request.getStatus() == Post.PostStatus.PUBLISHED && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
            post.setStatus(request.getStatus());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            post.setCategory(category);
        }

        if (request.getTags() != null) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            post.setTags(tags);
        }

        if (request.getAllowComment() != null) {
            post.setAllowComment(request.getAllowComment());
        }

        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }

    /**
     * Get post by ID
     */
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        return convertToResponse(post);
    }

    /**
     * Get post by slug
     */
    @Transactional(readOnly = true)
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        return convertToResponse(post);
    }

    /**
     * List all posts with pagination
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> listPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    /**
     * List published posts
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> listPublishedPosts(Pageable pageable) {
        return postRepository.findByStatus(Post.PostStatus.PUBLISHED, pageable)
                .map(this::convertToResponse);
    }

    /**
     * List posts by category
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> listPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Search posts by keyword
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(keyword, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Delete a post
     */
    @Transactional
    public void deletePost(Long id) {
        log.info("Deleting post: {}", id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        postRepository.delete(post);
    }

    /**
     * Increment view count
     */
    @Transactional
    public void incrementViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    /**
     * Get or create tags by name
     */
    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder()
                                .name(tagName)
                                .slug(generateSlug(tagName))
                                .build();
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }

    /**
     * Generate URL slug from title
     */
    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        
        // Ensure uniqueness
        String baseSlug = slug;
        int counter = 1;
        while (postRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        
        return slug;
    }

    /**
     * Convert Post entity to PostResponse DTO
     */
    private PostResponse convertToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(post.getContent())
                .thumbnail(post.getThumbnail())
                .status(post.getStatus())
                .authorName(post.getAuthor().getNickname() != null ? 
                        post.getAuthor().getNickname() : post.getAuthor().getUsername())
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
                .tags(post.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()))
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .allowComment(post.getAllowComment())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
