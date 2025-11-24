package com.aiproject.common.initializer;

import com.aiproject.module.category.model.Category;
import com.aiproject.module.category.repository.CategoryRepository;
import com.aiproject.module.post.model.Post;
import com.aiproject.module.post.repository.PostRepository;
import com.aiproject.module.tag.model.Tag;
import com.aiproject.module.tag.repository.TagRepository;
import com.aiproject.module.user.model.User;
import com.aiproject.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data Initializer
 * Populates the database with sample data on application startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping data initialization");
            return;
        }

        log.info("Initializing database with sample data...");

        // Create users
        User admin = User.builder()
                .username("admin")
                .password("{noop}admin123")  // {noop} prefix for plain text password in Spring Security
                .email("admin@blog.com")
                .nickname("管理员")
                .description("博客管理员")
                .role(User.UserRole.ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build();
        admin = userRepository.save(admin);
        log.info("Created admin user: {}", admin.getUsername());

        User author = User.builder()
                .username("author")
                .password("{noop}author123")
                .email("author@blog.com")
                .nickname("作者")
                .description("博客作者")
                .role(User.UserRole.AUTHOR)
                .status(User.UserStatus.ACTIVE)
                .build();
        author = userRepository.save(author);
        log.info("Created author user: {}", author.getUsername());

        // Create categories
        Category techCategory = Category.builder()
                .name("技术")
                .slug("tech")
                .description("技术相关文章")
                .build();
        techCategory = categoryRepository.save(techCategory);

        Category lifeCategory = Category.builder()
                .name("生活")
                .slug("life")
                .description("生活随笔")
                .build();
        lifeCategory = categoryRepository.save(lifeCategory);

        Category travelCategory = Category.builder()
                .name("旅行")
                .slug("travel")
                .description("旅行见闻")
                .build();
        travelCategory = categoryRepository.save(travelCategory);
        log.info("Created {} categories", categoryRepository.count());

        // Create tags
        Tag javaTag = Tag.builder()
                .name("Java")
                .slug("java")
                .build();
        javaTag = tagRepository.save(javaTag);

        Tag springTag = Tag.builder()
                .name("Spring Boot")
                .slug("spring-boot")
                .build();
        springTag = tagRepository.save(springTag);

        Tag blogTag = Tag.builder()
                .name("博客")
                .slug("blog")
                .build();
        blogTag = tagRepository.save(blogTag);
        log.info("Created {} tags", tagRepository.count());

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
                .author(admin)
                .category(techCategory)
                .tags(Set.of(blogTag, springTag))
                .publishedAt(LocalDateTime.now().minusDays(2))
                .viewCount(100L)
                .likeCount(15L)
                .commentCount(3)
                .build();
        postRepository.save(post1);

        Post post2 = Post.builder()
                .title("Spring Boot 博客系统开发指南")
                .slug("spring-boot-blog-development-guide")
                .summary("详细介绍如何使用 Spring Boot 构建一个功能完整的博客系统。")
                .content("<h2>Spring Boot 博客开发</h2>" +
                        "<p>本文将介绍如何使用 Spring Boot、JPA 和 H2 数据库构建博客系统。</p>" +
                        "<h3>技术栈:</h3>" +
                        "<ul><li>Spring Boot 3.1.5</li>" +
                        "<li>Spring Data JPA</li>" +
                        "<li>H2 Database</li>" +
                        "<li>Lombok</li></ul>")
                .originalContent("## Spring Boot 博客开发\n\n...")
                .status(Post.PostStatus.PUBLISHED)
                .author(author)
                .category(techCategory)
                .tags(Set.of(javaTag, springTag))
                .publishedAt(LocalDateTime.now().minusDays(1))
                .viewCount(250L)
                .likeCount(42L)
                .commentCount(8)
                .build();
        postRepository.save(post2);

        Post post3 = Post.builder()
                .title("我的第一次旅行")
                .slug("my-first-travel")
                .summary("分享我第一次独自旅行的经历和感受。")
                .content("<h2>旅行的意义</h2>" +
                        "<p>这是我第一次独自旅行，去了很多想去的地方，遇到了很多有趣的人。</p>" +
                        "<p>旅行不仅仅是看风景，更重要的是体验不同的文化和生活方式。</p>")
                .status(Post.PostStatus.PUBLISHED)
                .author(admin)
                .category(travelCategory)
                .tags(Set.of(blogTag))
                .publishedAt(LocalDateTime.now().minusHours(6))
                .viewCount(75L)
                .likeCount(10L)
                .commentCount(2)
                .build();
        postRepository.save(post3);

        Post draftPost = Post.builder()
                .title("即将发布的文章")
                .slug("upcoming-article")
                .summary("这篇文章还在编辑中...")
                .content("<p>内容编辑中...</p>")
                .status(Post.PostStatus.DRAFT)
                .author(author)
                .category(lifeCategory)
                .build();
        postRepository.save(draftPost);

        log.info("Created {} posts", postRepository.count());
        log.info("Database initialization completed successfully!");
    }
}
