package com.aiproject.common.initializer;

import com.aiproject.module.category.mapper.CategoryMapper;
import com.aiproject.module.category.model.Category;
import com.aiproject.module.post.mapper.PostMapper;
import com.aiproject.module.post.mapper.PostTagMapper;
import com.aiproject.module.post.model.Post;
import com.aiproject.module.post.model.PostTag;
import com.aiproject.module.tag.mapper.TagMapper;
import com.aiproject.module.tag.model.Tag;
import com.aiproject.module.user.mapper.UserMapper;
import com.aiproject.module.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data Initializer
 * Populates the database with sample data on application startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) {
            log.info("Database already initialized, skipping data initialization");
            return;
        }

        log.info("Initializing database with sample data...");

        LocalDateTime now = LocalDateTime.now();

        // Create users
        User admin = User.builder()
                .username("admin")
                .password("{noop}admin123")  // {noop} prefix for plain text password in Spring Security
                .email("admin@blog.com")
                .nickname("管理员")
                .description("博客管理员")
                .role(User.UserRole.ADMIN)
                .status(User.UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userMapper.insert(admin);
        log.info("Created admin user: {}", admin.getUsername());

        User author = User.builder()
                .username("author")
                .password("{noop}author123")
                .email("author@blog.com")
                .nickname("作者")
                .description("博客作者")
                .role(User.UserRole.AUTHOR)
                .status(User.UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userMapper.insert(author);
        log.info("Created author user: {}", author.getUsername());

        // Create categories
        Category techCategory = Category.builder()
                .name("技术")
                .slug("tech")
                .description("技术相关文章")
                .createdAt(now)
                .updatedAt(now)
                .build();
        categoryMapper.insert(techCategory);

        Category lifeCategory = Category.builder()
                .name("生活")
                .slug("life")
                .description("生活随笔")
                .createdAt(now)
                .updatedAt(now)
                .build();
        categoryMapper.insert(lifeCategory);

        Category travelCategory = Category.builder()
                .name("旅行")
                .slug("travel")
                .description("旅行见闻")
                .createdAt(now)
                .updatedAt(now)
                .build();
        categoryMapper.insert(travelCategory);
        log.info("Created {} categories", categoryMapper.selectCount(null));

        // Create tags
        Tag javaTag = Tag.builder()
                .name("Java")
                .slug("java")
                .createdAt(now)
                .build();
        tagMapper.insert(javaTag);

        Tag springTag = Tag.builder()
                .name("Spring Boot")
                .slug("spring-boot")
                .createdAt(now)
                .build();
        tagMapper.insert(springTag);

        Tag blogTag = Tag.builder()
                .name("博客")
                .slug("blog")
                .createdAt(now)
                .build();
        tagMapper.insert(blogTag);
        log.info("Created {} tags", tagMapper.selectCount(null));

        // Create sample posts
        Post post1 = Post.builder()
                .title("欢迎来到我的博客")
                .slug("welcome-to-my-blog")
                .summary("这是第一篇博客文章，介绍了博客的基本功能和使用方法。")
                .content("<h2>欢迎!</h2><p>这是一个基于 Halo 架构设计的现代化博客系统。" +
                        "它提供了文章管理、分类标签、评论系统等完整的博客功能。</p>" +
                        "<h3>主要特性:</h3><ul>" +
                        "<li>文章管理 - 支持创建、编辑、删除文章</li>" +
                        "<li>分类和标签 - 组织和筛选文章</li>" +
                        "<li>评论系统 - 用户可以评论文章</li>" +
                        "<li>用户管理 - 多角色支持</li>" +
                        "</ul>")
                .originalContent("# 欢迎!\n\n这是一个基于 Halo 架构设计的现代化博客系统...")
                .status(Post.PostStatus.PUBLISHED)
                .authorId(admin.getId())
                .categoryId(techCategory.getId())
                .publishedAt(now.minusDays(2))
                .viewCount(100L)
                .likeCount(15L)
                .commentCount(3)
                .createdAt(now)
                .updatedAt(now)
                .build();
        postMapper.insert(post1);
        
        // Add tags for post1
        postTagMapper.insert(PostTag.builder().postId(post1.getId()).tagId(blogTag.getId()).build());
        postTagMapper.insert(PostTag.builder().postId(post1.getId()).tagId(springTag.getId()).build());

        Post post2 = Post.builder()
                .title("Spring Boot 博客系统开发指南")
                .slug("spring-boot-blog-development-guide")
                .summary("详细介绍如何使用 Spring Boot 构建一个功能完整的博客系统。")
                .content("<h2>Spring Boot 博客开发</h2>" +
                        "<p>本文将介绍如何使用 Spring Boot、MyBatis Plus 和 MySQL 数据库构建博客系统。</p>" +
                        "<h3>技术栈:</h3>" +
                        "<ul><li>Spring Boot 3.1.5</li>" +
                        "<li>MyBatis Plus</li>" +
                        "<li>MySQL Database</li>" +
                        "<li>Lombok</li></ul>")
                .originalContent("## Spring Boot 博客开发\n\n...")
                .status(Post.PostStatus.PUBLISHED)
                .authorId(author.getId())
                .categoryId(techCategory.getId())
                .publishedAt(now.minusDays(1))
                .viewCount(250L)
                .likeCount(42L)
                .commentCount(8)
                .createdAt(now)
                .updatedAt(now)
                .build();
        postMapper.insert(post2);
        
        // Add tags for post2
        postTagMapper.insert(PostTag.builder().postId(post2.getId()).tagId(javaTag.getId()).build());
        postTagMapper.insert(PostTag.builder().postId(post2.getId()).tagId(springTag.getId()).build());

        Post post3 = Post.builder()
                .title("我的第一次旅行")
                .slug("my-first-travel")
                .summary("分享我第一次独自旅行的经历和感受。")
                .content("<h2>旅行的意义</h2>" +
                        "<p>这是我第一次独自旅行，去了很多想去的地方，遇到了很多有趣的人。</p>" +
                        "<p>旅行不仅仅是看风景，更重要的是体验不同的文化和生活方式。</p>")
                .status(Post.PostStatus.PUBLISHED)
                .authorId(admin.getId())
                .categoryId(travelCategory.getId())
                .publishedAt(now.minusHours(6))
                .viewCount(75L)
                .likeCount(10L)
                .commentCount(2)
                .createdAt(now)
                .updatedAt(now)
                .build();
        postMapper.insert(post3);
        
        // Add tags for post3
        postTagMapper.insert(PostTag.builder().postId(post3.getId()).tagId(blogTag.getId()).build());

        Post draftPost = Post.builder()
                .title("即将发布的文章")
                .slug("upcoming-article")
                .summary("这篇文章还在编辑中...")
                .content("<p>内容编辑中...</p>")
                .status(Post.PostStatus.DRAFT)
                .authorId(author.getId())
                .categoryId(lifeCategory.getId())
                .createdAt(now)
                .updatedAt(now)
                .build();
        postMapper.insert(draftPost);

        log.info("Created {} posts", postMapper.selectCount(null));
        log.info("Database initialization completed successfully!");
    }
}
