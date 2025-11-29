package com.aiproject.module.post.service;

import com.aiproject.module.category.mapper.CategoryMapper;
import com.aiproject.module.category.model.Category;
import com.aiproject.module.post.mapper.PostMapper;
import com.aiproject.module.post.mapper.PostTagMapper;
import com.aiproject.module.post.model.Post;
import com.aiproject.module.post.model.PostRequest;
import com.aiproject.module.post.model.PostResponse;
import com.aiproject.module.post.model.PostTag;
import com.aiproject.module.tag.mapper.TagMapper;
import com.aiproject.module.tag.model.Tag;
import com.aiproject.module.user.mapper.UserMapper;
import com.aiproject.module.user.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Post Service
 * Handles blog post business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService extends ServiceImpl<PostMapper, Post> {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;

    /**
     * Create a new post
     */
    @Transactional
    public PostResponse createPost(PostRequest request, Long authorId) {
        log.info("Creating new post: {}", request.getTitle());

        User author = userMapper.selectById(authorId);
        if (author == null) {
            throw new RuntimeException("Author not found");
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .summary(request.getSummary())
                .content(request.getContent())
                .originalContent(request.getOriginalContent())
                .thumbnail(request.getThumbnail())
                .status(request.getStatus() != null ? request.getStatus() : Post.PostStatus.DRAFT)
                .authorId(authorId)
                .allowComment(request.getAllowComment() != null ? request.getAllowComment() : true)
                
                
                .build();

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new RuntimeException("Category not found");
            }
            post.setCategoryId(request.getCategoryId());
        }

        // Set published date if status is PUBLISHED
        if (post.getStatus() == Post.PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }

        postMapper.insert(post);

        // Set tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            for (Tag tag : tags) {
                PostTag postTag = PostTag.builder()
                        .postId(post.getId())
                        .tagId(tag.getId())
                        .build();
                postTagMapper.insert(postTag);
            }
        }

        return convertToResponse(post, author, 
                request.getCategoryId() != null ? categoryMapper.selectById(request.getCategoryId()) : null,
                getTagsForPost(post.getId()));
    }

    /**
     * Update an existing post
     */
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        log.info("Updating post: {}", id);

        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("Post not found");
        }

        post.setTitle(request.getTitle());
        post.setSlug(generateSlug(request.getTitle()));
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setOriginalContent(request.getOriginalContent());
        post.setThumbnail(request.getThumbnail());
        

        if (request.getStatus() != null) {
            if (request.getStatus() == Post.PostStatus.PUBLISHED && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
            post.setStatus(request.getStatus());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new RuntimeException("Category not found");
            }
            post.setCategoryId(request.getCategoryId());
        }

        if (request.getAllowComment() != null) {
            post.setAllowComment(request.getAllowComment());
        }

        postMapper.updateById(post);

        // Update tags
        if (request.getTags() != null) {
            // Delete old tags
            postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, id));
            // Add new tags
            Set<Tag> tags = getOrCreateTags(request.getTags());
            for (Tag tag : tags) {
                PostTag postTag = PostTag.builder()
                        .postId(post.getId())
                        .tagId(tag.getId())
                        .build();
                postTagMapper.insert(postTag);
            }
        }

        User author = userMapper.selectById(post.getAuthorId());
        Category category = post.getCategoryId() != null ? categoryMapper.selectById(post.getCategoryId()) : null;
        return convertToResponse(post, author, category, getTagsForPost(post.getId()));
    }

    /**
     * Get post by ID
     */
    public PostResponse getPostById(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("Post not found");
        }
        User author = userMapper.selectById(post.getAuthorId());
        Category category = post.getCategoryId() != null ? categoryMapper.selectById(post.getCategoryId()) : null;
        return convertToResponse(post, author, category, getTagsForPost(post.getId()));
    }

    /**
     * Get post by slug
     */
    public PostResponse getPostBySlug(String slug) {
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getSlug, slug));
        if (post == null) {
            throw new RuntimeException("Post not found");
        }
        User author = userMapper.selectById(post.getAuthorId());
        Category category = post.getCategoryId() != null ? categoryMapper.selectById(post.getCategoryId()) : null;
        return convertToResponse(post, author, category, getTagsForPost(post.getId()));
    }

    /**
     * List all posts with pagination
     */
    public IPage<PostResponse> listPosts(int page, int size) {
        Page<Post> postPage = new Page<>(page + 1, size);
        IPage<Post> result = postMapper.selectPage(postPage, 
                new LambdaQueryWrapper<Post>().orderByDesc(Post::getCreatedAt));
        
        return result.convert(post -> {
            User author = userMapper.selectById(post.getAuthorId());
            Category category = post.getCategoryId() != null ? categoryMapper.selectById(post.getCategoryId()) : null;
            return convertToResponse(post, author, category, getTagsForPost(post.getId()));
        });
    }

    /**
     * List published posts
     */
    public IPage<PostResponse> listPublishedPosts(int page, int size) {
        Page<Post> postPage = new Page<>(page + 1, size);
        IPage<Post> result = postMapper.selectPage(postPage,
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, Post.PostStatus.PUBLISHED)
                        .orderByDesc(Post::getPublishedAt));
        
        return result.convert(post -> {
            User author = userMapper.selectById(post.getAuthorId());
            Category category = post.getCategoryId() != null ? categoryMapper.selectById(post.getCategoryId()) : null;
            return convertToResponse(post, author, category, getTagsForPost(post.getId()));
        });
    }

    /**
     * List posts by category
     */
    public IPage<PostResponse> listPostsByCategory(Long categoryId, int page, int size) {
        Page<Post> postPage = new Page<>(page + 1, size);
        IPage<Post> result = postMapper.selectPage(postPage,
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getCategoryId, categoryId)
                        .orderByDesc(Post::getCreatedAt));
        
        return result.convert(post -> {
            User author = userMapper.selectById(post.getAuthorId());
            Category category = categoryMapper.selectById(categoryId);
            return convertToResponse(post, author, category, getTagsForPost(post.getId()));
        });
    }

    /**
     * List posts by tag ID
     */
    public IPage<PostResponse> listPostsByTag(Long tagId, int page, int size) {
        // First, get all post IDs associated with this tag
        List<PostTag> postTags = postTagMapper.selectList(
                new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, tagId));
        
        if (postTags.isEmpty()) {
            // Return empty page if no posts with this tag
            Page<PostResponse> emptyPage = new Page<>(page + 1, size);
            emptyPage.setRecords(new ArrayList<>());
            emptyPage.setTotal(0);
            return emptyPage;
        }
        
        List<Long> postIds = postTags.stream().map(PostTag::getPostId).collect(Collectors.toList());
        
        // Then paginate the posts
        Page<Post> postPage = new Page<>(page + 1, size);
        IPage<Post> result = postMapper.selectPage(postPage,
                new LambdaQueryWrapper<Post>()
                        .in(Post::getId, postIds)
                        .orderByDesc(Post::getCreatedAt));
        
        return result.convert(post -> {
            User author = userMapper.selectById(post.getAuthorId());
            Category category = post.getCategoryId() != null ? categoryMapper.selectById(post.getCategoryId()) : null;
            return convertToResponse(post, author, category, getTagsForPost(post.getId()));
        });
    }

    /**
     * Search posts by keyword
     */
    public IPage<PostResponse> searchPosts(String keyword, int page, int size) {
        Page<Post> postPage = new Page<>(page + 1, size);
        IPage<Post> result = postMapper.selectPage(postPage,
                new LambdaQueryWrapper<Post>()
                        .like(Post::getTitle, keyword)
                        .orderByDesc(Post::getCreatedAt));
        
        return result.convert(post -> {
            User author = userMapper.selectById(post.getAuthorId());
            Category category = post.getCategoryId() != null ? categoryMapper.selectById(post.getCategoryId()) : null;
            return convertToResponse(post, author, category, getTagsForPost(post.getId()));
        });
    }

    /**
     * Delete a post
     */
    @Transactional
    public void deletePost(Long id) {
        log.info("Deleting post: {}", id);
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("Post not found");
        }
        // Delete post tags
        postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, id));
        postMapper.deleteById(id);
    }

    /**
     * Increment view count
     */
    @Transactional
    public void incrementViewCount(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("Post not found");
        }
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);
    }

    /**
     * Get tags for a post
     */
    private Set<Tag> getTagsForPost(Long postId) {
        List<PostTag> postTags = postTagMapper.selectList(
                new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId));
        Set<Tag> tags = new HashSet<>();
        for (PostTag postTag : postTags) {
            Tag tag = tagMapper.selectById(postTag.getTagId());
            if (tag != null) {
                tags.add(tag);
            }
        }
        return tags;
    }

    /**
     * Get or create tags by name
     */
    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tagName));
            if (tag == null) {
                tag = Tag.builder()
                        .name(tagName)
                        .slug(generateTagSlug(tagName))
                        
                        .build();
                tagMapper.insert(tag);
            }
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
        
        String baseSlug = slug;
        int counter = 1;
        while (postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getSlug, slug)) > 0) {
            slug = baseSlug + "-" + counter++;
        }
        
        return slug;
    }

    private String generateTagSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    /**
     * Convert Post entity to PostResponse DTO
     */
    private PostResponse convertToResponse(Post post, User author, Category category, Set<Tag> tags) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(post.getContent())
                .thumbnail(post.getThumbnail())
                .status(post.getStatus())
                .authorName(author != null ? (author.getNickname() != null ? author.getNickname() : author.getUsername()) : null)
                .categoryName(category != null ? category.getName() : null)
                .tags(tags.stream().map(Tag::getName).collect(Collectors.toSet()))
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
