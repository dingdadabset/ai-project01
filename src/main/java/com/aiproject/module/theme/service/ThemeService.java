package com.aiproject.module.theme.service;

import com.aiproject.module.theme.mapper.ThemeMapper;
import com.aiproject.module.theme.model.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Theme Service
 * Handles theme management, installation, and rendering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeService extends ServiceImpl<ThemeMapper, Theme> {

    private final ThemeMapper themeMapper;
    private final ObjectMapper objectMapper;

    @Value("${theme.directory:themes}")
    private String themeDirectory;

    @Value("${theme.default:default}")
    private String defaultThemeName;

    /**
     * Initialize theme system on startup
     */
    @PostConstruct
    public void init() {
        log.info("Initializing theme system...");
        try {
            Path themesPath = getThemesPath();
            if (!Files.exists(themesPath)) {
                Files.createDirectories(themesPath);
                log.info("Created themes directory: {}", themesPath);
            }
            
            // Ensure default theme exists
            initializeDefaultTheme();
            
            // Scan and register all themes
            scanAndRegisterThemes();
            
            log.info("Theme system initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize theme system", e);
        }
    }

    /**
     * Get all themes
     */
    public List<ThemeResponse> listThemes() {
        List<Theme> themes = themeMapper.selectList(
            new LambdaQueryWrapper<Theme>().orderByDesc(Theme::getIsActive).orderByAsc(Theme::getName)
        );
        return themes.stream().map(this::convertToResponse).toList();
    }

    /**
     * Get theme by ID
     */
    public ThemeResponse getThemeById(Long id) {
        Theme theme = themeMapper.selectById(id);
        if (theme == null) {
            throw new RuntimeException("Theme not found");
        }
        return convertToResponse(theme);
    }

    /**
     * Get theme by theme ID (folder name)
     */
    public ThemeResponse getThemeByThemeId(String themeId) {
        Theme theme = themeMapper.selectOne(
            new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, themeId)
        );
        if (theme == null) {
            throw new RuntimeException("Theme not found: " + themeId);
        }
        return convertToResponse(theme);
    }

    /**
     * Get currently active theme
     */
    public ThemeResponse getActiveTheme() {
        Theme theme = themeMapper.selectOne(
            new LambdaQueryWrapper<Theme>().eq(Theme::getIsActive, true)
        );
        if (theme == null) {
            // Fallback to default theme
            theme = themeMapper.selectOne(
                new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, defaultThemeName)
            );
        }
        if (theme == null) {
            throw new RuntimeException("No active theme found");
        }
        return convertToResponse(theme);
    }

    /**
     * Activate a theme
     */
    @Transactional
    public ThemeResponse activateTheme(String themeId) {
        log.info("Activating theme: {}", themeId);
        
        Theme theme = themeMapper.selectOne(
            new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, themeId)
        );
        if (theme == null) {
            throw new RuntimeException("Theme not found: " + themeId);
        }
        
        if (theme.getStatus() != Theme.ThemeStatus.ENABLED) {
            throw new RuntimeException("Theme is not enabled: " + themeId);
        }
        
        // Deactivate all themes
        themeMapper.selectList(new LambdaQueryWrapper<Theme>().eq(Theme::getIsActive, true))
            .forEach(t -> {
                t.setIsActive(false);
                themeMapper.updateById(t);
            });
        
        // Activate selected theme
        theme.setIsActive(true);
        themeMapper.updateById(theme);
        
        return convertToResponse(theme);
    }

    /**
     * Enable a theme
     */
    @Transactional
    public ThemeResponse enableTheme(String themeId) {
        Theme theme = themeMapper.selectOne(
            new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, themeId)
        );
        if (theme == null) {
            throw new RuntimeException("Theme not found: " + themeId);
        }
        
        theme.setStatus(Theme.ThemeStatus.ENABLED);
        themeMapper.updateById(theme);
        
        return convertToResponse(theme);
    }

    /**
     * Disable a theme
     */
    @Transactional
    public ThemeResponse disableTheme(String themeId) {
        Theme theme = themeMapper.selectOne(
            new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, themeId)
        );
        if (theme == null) {
            throw new RuntimeException("Theme not found: " + themeId);
        }
        
        if (theme.getIsActive()) {
            throw new RuntimeException("Cannot disable active theme");
        }
        
        theme.setStatus(Theme.ThemeStatus.DISABLED);
        themeMapper.updateById(theme);
        
        return convertToResponse(theme);
    }

    /**
     * Update theme settings
     */
    @Transactional
    public ThemeResponse updateThemeSettings(String themeId, Map<String, Object> settings) {
        Theme theme = themeMapper.selectOne(
            new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, themeId)
        );
        if (theme == null) {
            throw new RuntimeException("Theme not found: " + themeId);
        }
        
        try {
            theme.setSettingsJson(objectMapper.writeValueAsString(settings));
            themeMapper.updateById(theme);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize settings", e);
        }
        
        return convertToResponse(theme);
    }

    /**
     * Install theme from ZIP file
     */
    @Transactional
    public ThemeResponse installTheme(MultipartFile file) {
        log.info("Installing theme from file: {}", file.getOriginalFilename());
        
        try {
            Path tempDir = Files.createTempDirectory("theme-install-");
            Path zipFile = tempDir.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), zipFile);
            
            // Extract ZIP
            Path extractDir = tempDir.resolve("extracted");
            Files.createDirectories(extractDir);
            unzip(zipFile, extractDir);
            
            // Find theme.yaml
            Path themeYamlPath = findThemeYaml(extractDir);
            if (themeYamlPath == null) {
                throw new RuntimeException("Invalid theme package: theme.yaml not found");
            }
            
            // Parse theme.yaml
            ThemeConfig config = parseThemeConfig(themeYamlPath);
            if (config.getId() == null || config.getId().isEmpty()) {
                throw new RuntimeException("Invalid theme: id is required in theme.yaml");
            }
            
            // Check if theme already exists
            Theme existingTheme = themeMapper.selectOne(
                new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, config.getId())
            );
            if (existingTheme != null) {
                throw new RuntimeException("Theme already exists: " + config.getId());
            }
            
            // Copy theme to themes directory
            Path themeSourceDir = themeYamlPath.getParent();
            Path themeTargetDir = getThemesPath().resolve(config.getId());
            copyDirectory(themeSourceDir, themeTargetDir);
            
            // Register theme in database
            Theme theme = createThemeFromConfig(config);
            themeMapper.insert(theme);
            
            // Cleanup temp files
            deleteDirectory(tempDir);
            
            return convertToResponse(theme);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to install theme", e);
        }
    }

    /**
     * Delete a theme
     */
    @Transactional
    public void deleteTheme(String themeId) {
        log.info("Deleting theme: {}", themeId);
        
        if (defaultThemeName.equals(themeId)) {
            throw new RuntimeException("Cannot delete default theme");
        }
        
        Theme theme = themeMapper.selectOne(
            new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, themeId)
        );
        if (theme == null) {
            throw new RuntimeException("Theme not found: " + themeId);
        }
        
        if (theme.getIsActive()) {
            throw new RuntimeException("Cannot delete active theme");
        }
        
        // Delete from database
        themeMapper.deleteById(theme.getId());
        
        // Delete theme files
        try {
            Path themePath = getThemesPath().resolve(themeId);
            if (Files.exists(themePath)) {
                deleteDirectory(themePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete theme files: {}", themeId, e);
        }
    }

    /**
     * Get theme settings schema (for form generation)
     */
    public ThemeConfig getThemeSettingsSchema(String themeId) {
        try {
            Path themePath = getThemesPath().resolve(themeId);
            Path configPath = themePath.resolve("theme.yaml");
            if (!Files.exists(configPath)) {
                configPath = themePath.resolve("theme.yml");
            }
            if (!Files.exists(configPath)) {
                throw new RuntimeException("Theme configuration not found: " + themeId);
            }
            return parseThemeConfig(configPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read theme configuration", e);
        }
    }

    /**
     * Get theme template path
     */
    public Path getTemplatePath(String themeId, String templateName) {
        return getThemesPath().resolve(themeId).resolve("templates").resolve(templateName + ".html");
    }

    /**
     * Get theme static resource path
     */
    public Path getStaticPath(String themeId) {
        return getThemesPath().resolve(themeId).resolve("static");
    }

    /**
     * Render a template with context
     */
    public String renderTemplate(String themeId, String templateName, ThemeContext context) {
        Path templatePath = getTemplatePath(themeId, templateName);
        if (!Files.exists(templatePath)) {
            throw new RuntimeException("Template not found: " + templateName);
        }
        
        // The actual rendering will be done by Thymeleaf through the controller
        // This method can be used for custom rendering if needed
        return templatePath.toString();
    }

    // ==================== Private Helper Methods ====================

    private Path getThemesPath() {
        return Paths.get(System.getProperty("user.dir"), themeDirectory);
    }

    private void initializeDefaultTheme() throws IOException {
        Path defaultThemePath = getThemesPath().resolve(defaultThemeName);
        if (!Files.exists(defaultThemePath)) {
            log.info("Creating default theme...");
            createDefaultTheme(defaultThemePath);
        }
    }

    private void createDefaultTheme(Path themePath) throws IOException {
        Files.createDirectories(themePath);
        Files.createDirectories(themePath.resolve("templates"));
        Files.createDirectories(themePath.resolve("static/css"));
        Files.createDirectories(themePath.resolve("static/js"));
        Files.createDirectories(themePath.resolve("static/images"));
        Files.createDirectories(themePath.resolve("i18n"));

        // Create theme.yaml
        String themeYaml = """
            id: default
            name: Default Theme
            version: 1.0.0
            author:
              name: AI Project Team
              website: https://github.com/aiproject
            description: A clean and responsive default theme for the blog
            screenshot: screenshot.png
            requires: "1.0.0"
            
            settings:
              - group: general
                label: General Settings
                items:
                  - name: siteName
                    label: Site Name
                    type: text
                    defaultValue: My Blog
                  - name: siteDescription
                    label: Site Description
                    type: textarea
                    defaultValue: A personal blog
              - group: appearance
                label: Appearance
                items:
                  - name: darkMode
                    label: Enable Dark Mode
                    type: switch
                    defaultValue: true
                  - name: primaryColor
                    label: Primary Color
                    type: color
                    defaultValue: "#6366f1"
                  - name: fontSize
                    label: Base Font Size
                    type: select
                    defaultValue: medium
                    options:
                      - label: Small
                        value: small
                      - label: Medium
                        value: medium
                      - label: Large
                        value: large
              - group: sidebar
                label: Sidebar
                items:
                  - name: showSidebar
                    label: Show Sidebar
                    type: switch
                    defaultValue: true
                  - name: showCategories
                    label: Show Categories Widget
                    type: switch
                    defaultValue: true
                  - name: showTags
                    label: Show Tags Widget
                    type: switch
                    defaultValue: true
                  - name: showRecentPosts
                    label: Show Recent Posts Widget
                    type: switch
                    defaultValue: true
            
            i18n:
              defaultLocale: en
              supportedLocales:
                - en
                - zh-CN
            
            features:
              darkMode: true
              responsive: true
              pwa: false
              comments: true
              search: true
            """;
        Files.writeString(themePath.resolve("theme.yaml"), themeYaml);

        // Create base layout template
        String layoutHtml = """
            <!DOCTYPE html>
            <html xmlns:th="http://www.thymeleaf.org" th:lang="${locale}">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title th:text="${site.title + ' - ' + (pageTitle != null ? pageTitle : 'Home')}">Blog</title>
                <link rel="stylesheet" th:href="@{/themes/default/static/css/style.css}">
                <th:block th:replace="~{fragments/head :: extra-head}"></th:block>
            </head>
            <body th:class="${settings.darkMode ? 'dark-mode' : ''}">
                <header class="site-header">
                    <nav class="navbar">
                        <a th:href="@{/}" class="logo" th:text="${site.title}">Blog</a>
                        <ul class="nav-menu">
                            <li><a th:href="@{/}">Home</a></li>
                            <li><a th:href="@{/posts}">Posts</a></li>
                            <li><a th:href="@{/categories}">Categories</a></li>
                            <li><a th:href="@{/tags}">Tags</a></li>
                            <li><a th:href="@{/about}">About</a></li>
                        </ul>
                    </nav>
                </header>
                
                <main class="main-content">
                    <div class="container">
                        <div class="content-wrapper" th:classappend="${settings.showSidebar ? 'with-sidebar' : ''}">
                            <div class="content">
                                <th:block th:replace="~{__${template}__ :: content}"></th:block>
                            </div>
                            <aside class="sidebar" th:if="${settings.showSidebar}">
                                <th:block th:replace="~{fragments/sidebar :: sidebar}"></th:block>
                            </aside>
                        </div>
                    </div>
                </main>
                
                <footer class="site-footer">
                    <div class="container">
                        <p th:text="${site.copyright != null ? site.copyright : '© ' + #dates.year(#dates.createNow()) + ' ' + site.title}"></p>
                    </div>
                </footer>
                
                <script th:src="@{/themes/default/static/js/main.js}"></script>
            </body>
            </html>
            """;
        Files.writeString(themePath.resolve("templates/layout.html"), layoutHtml);

        // Create index template
        String indexHtml = """
            <!DOCTYPE html>
            <html xmlns:th="http://www.thymeleaf.org" th:replace="~{layout :: document(~{::content}, 'Home')}">
            <th:block th:fragment="content">
                <section class="posts-section">
                    <h1 th:text="#{index.latestPosts}">Latest Posts</h1>
                    <div class="posts-grid">
                        <article class="post-card" th:each="post : ${posts}">
                            <img th:if="${post.thumbnail}" th:src="${post.thumbnail}" alt="" class="post-thumbnail">
                            <div class="post-content">
                                <h2><a th:href="@{/posts/{slug}(slug=${post.slug})}" th:text="${post.title}">Post Title</a></h2>
                                <p class="post-meta">
                                    <span th:text="${post.authorName}">Author</span>
                                    <span th:text="${post.publishedAt}">Date</span>
                                </p>
                                <p class="post-summary" th:text="${post.summary}">Summary...</p>
                                <div class="post-tags" th:if="${post.tags}">
                                    <span class="tag" th:each="tag : ${post.tags}" th:text="${tag}">Tag</span>
                                </div>
                            </div>
                        </article>
                    </div>
                    
                    <nav class="pagination" th:if="${pagination != null and pagination.pages > 1}">
                        <a th:if="${pagination.hasPrevious}" th:href="${pagination.previousUrl}" class="page-link">Previous</a>
                        <span th:text="${pagination.current + 1} + ' / ' + ${pagination.pages}">1 / 10</span>
                        <a th:if="${pagination.hasNext}" th:href="${pagination.nextUrl}" class="page-link">Next</a>
                    </nav>
                </section>
            </th:block>
            </html>
            """;
        Files.writeString(themePath.resolve("templates/index.html"), indexHtml);

        // Create post template
        String postHtml = """
            <!DOCTYPE html>
            <html xmlns:th="http://www.thymeleaf.org" th:replace="~{layout :: document(~{::content}, ${post.title})}">
            <th:block th:fragment="content">
                <article class="post-single">
                    <header class="post-header">
                        <h1 th:text="${post.title}">Post Title</h1>
                        <div class="post-meta">
                            <span class="author" th:text="${post.authorName}">Author</span>
                            <span class="date" th:text="${post.publishedAt}">Date</span>
                            <span class="category" th:if="${post.categoryName}" th:text="${post.categoryName}">Category</span>
                            <span class="views" th:text="'Views: ' + ${post.viewCount}">Views</span>
                        </div>
                    </header>
                    
                    <img th:if="${post.thumbnail}" th:src="${post.thumbnail}" alt="" class="post-featured-image">
                    
                    <div class="post-content" th:utext="${post.content}">
                        Post content goes here...
                    </div>
                    
                    <footer class="post-footer">
                        <div class="post-tags" th:if="${post.tags}">
                            <span th:text="#{post.tags}">Tags:</span>
                            <a th:each="tag : ${post.tags}" th:href="@{/tags/{slug}(slug=${tag})}" th:text="${tag}">Tag</a>
                        </div>
                    </footer>
                    
                    <section class="comments-section" th:if="${post.allowComment}">
                        <h3 th:text="#{post.comments}">Comments</h3>
                        <div id="comments-container">
                            <!-- Comments will be loaded dynamically -->
                        </div>
                    </section>
                </article>
            </th:block>
            </html>
            """;
        Files.writeString(themePath.resolve("templates/post.html"), postHtml);

        // Create fragments
        Files.createDirectories(themePath.resolve("templates/fragments"));
        
        String sidebarHtml = """
            <th:block xmlns:th="http://www.thymeleaf.org" th:fragment="sidebar">
                <div class="widget categories-widget" th:if="${settings.showCategories}">
                    <h3 th:text="#{sidebar.categories}">Categories</h3>
                    <ul>
                        <li th:each="category : ${categories}">
                            <a th:href="@{/categories/{id}(id=${category.id})}" th:text="${category.name + ' (' + category.postCount + ')'}">Category</a>
                        </li>
                    </ul>
                </div>
                
                <div class="widget tags-widget" th:if="${settings.showTags}">
                    <h3 th:text="#{sidebar.tags}">Tags</h3>
                    <div class="tag-cloud">
                        <a th:each="tag : ${tags}" th:href="@{/tags/{slug}(slug=${tag.slug})}" class="tag" th:text="${tag.name}">Tag</a>
                    </div>
                </div>
                
                <div class="widget recent-posts-widget" th:if="${settings.showRecentPosts}">
                    <h3 th:text="#{sidebar.recentPosts}">Recent Posts</h3>
                    <ul>
                        <li th:each="post : ${recentPosts}">
                            <a th:href="@{/posts/{slug}(slug=${post.slug})}" th:text="${post.title}">Post</a>
                        </li>
                    </ul>
                </div>
            </th:block>
            """;
        Files.writeString(themePath.resolve("templates/fragments/sidebar.html"), sidebarHtml);

        String headHtml = """
            <th:block xmlns:th="http://www.thymeleaf.org" th:fragment="extra-head">
                <!-- Additional head content -->
            </th:block>
            """;
        Files.writeString(themePath.resolve("templates/fragments/head.html"), headHtml);

        // Create CSS
        String styleCss = """
            /* Default Theme Styles */
            :root {
                --primary-color: #6366f1;
                --secondary-color: #ec4899;
                --text-color: #1f2937;
                --text-muted: #6b7280;
                --bg-color: #ffffff;
                --bg-secondary: #f3f4f6;
                --border-color: #e5e7eb;
                --font-size-base: 16px;
            }
            
            .dark-mode {
                --text-color: #f9fafb;
                --text-muted: #9ca3af;
                --bg-color: #111827;
                --bg-secondary: #1f2937;
                --border-color: #374151;
            }
            
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }
            
            html {
                font-size: var(--font-size-base);
            }
            
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                background: var(--bg-color);
                color: var(--text-color);
                line-height: 1.6;
            }
            
            .container {
                max-width: 1200px;
                margin: 0 auto;
                padding: 0 20px;
            }
            
            /* Header */
            .site-header {
                background: var(--bg-secondary);
                border-bottom: 1px solid var(--border-color);
                padding: 1rem 0;
            }
            
            .navbar {
                display: flex;
                justify-content: space-between;
                align-items: center;
                max-width: 1200px;
                margin: 0 auto;
                padding: 0 20px;
            }
            
            .logo {
                font-size: 1.5rem;
                font-weight: bold;
                color: var(--primary-color);
                text-decoration: none;
            }
            
            .nav-menu {
                display: flex;
                list-style: none;
                gap: 2rem;
            }
            
            .nav-menu a {
                color: var(--text-color);
                text-decoration: none;
                transition: color 0.2s;
            }
            
            .nav-menu a:hover {
                color: var(--primary-color);
            }
            
            /* Main Content */
            .main-content {
                min-height: calc(100vh - 200px);
                padding: 2rem 0;
            }
            
            .content-wrapper {
                display: grid;
                gap: 2rem;
            }
            
            .content-wrapper.with-sidebar {
                grid-template-columns: 1fr 300px;
            }
            
            /* Posts */
            .posts-grid {
                display: grid;
                gap: 2rem;
            }
            
            .post-card {
                background: var(--bg-secondary);
                border-radius: 12px;
                overflow: hidden;
                transition: transform 0.2s, box-shadow 0.2s;
            }
            
            .post-card:hover {
                transform: translateY(-4px);
                box-shadow: 0 10px 20px rgba(0,0,0,0.1);
            }
            
            .post-thumbnail {
                width: 100%;
                height: 200px;
                object-fit: cover;
            }
            
            .post-card .post-content {
                padding: 1.5rem;
            }
            
            .post-card h2 {
                margin-bottom: 0.5rem;
            }
            
            .post-card h2 a {
                color: var(--text-color);
                text-decoration: none;
            }
            
            .post-card h2 a:hover {
                color: var(--primary-color);
            }
            
            .post-meta {
                color: var(--text-muted);
                font-size: 0.875rem;
                margin-bottom: 0.5rem;
            }
            
            .post-meta span {
                margin-right: 1rem;
            }
            
            .post-summary {
                color: var(--text-muted);
            }
            
            .post-tags {
                margin-top: 1rem;
                display: flex;
                flex-wrap: wrap;
                gap: 0.5rem;
            }
            
            .tag {
                display: inline-block;
                padding: 0.25rem 0.75rem;
                background: var(--primary-color);
                color: white;
                border-radius: 20px;
                font-size: 0.75rem;
                text-decoration: none;
            }
            
            /* Single Post */
            .post-single {
                max-width: 800px;
            }
            
            .post-header {
                margin-bottom: 2rem;
            }
            
            .post-header h1 {
                font-size: 2.5rem;
                margin-bottom: 1rem;
            }
            
            .post-featured-image {
                width: 100%;
                border-radius: 12px;
                margin-bottom: 2rem;
            }
            
            .post-content {
                line-height: 1.8;
            }
            
            .post-content img {
                max-width: 100%;
                border-radius: 8px;
            }
            
            .post-content h2, .post-content h3, .post-content h4 {
                margin: 1.5rem 0 1rem;
            }
            
            .post-content p {
                margin-bottom: 1rem;
            }
            
            .post-content pre {
                background: var(--bg-secondary);
                padding: 1rem;
                border-radius: 8px;
                overflow-x: auto;
            }
            
            .post-content code {
                font-family: 'Fira Code', monospace;
            }
            
            /* Sidebar */
            .sidebar {
                position: sticky;
                top: 2rem;
            }
            
            .widget {
                background: var(--bg-secondary);
                padding: 1.5rem;
                border-radius: 12px;
                margin-bottom: 1.5rem;
            }
            
            .widget h3 {
                margin-bottom: 1rem;
                padding-bottom: 0.5rem;
                border-bottom: 2px solid var(--primary-color);
            }
            
            .widget ul {
                list-style: none;
            }
            
            .widget li {
                padding: 0.5rem 0;
                border-bottom: 1px solid var(--border-color);
            }
            
            .widget li:last-child {
                border-bottom: none;
            }
            
            .widget a {
                color: var(--text-color);
                text-decoration: none;
            }
            
            .widget a:hover {
                color: var(--primary-color);
            }
            
            .tag-cloud {
                display: flex;
                flex-wrap: wrap;
                gap: 0.5rem;
            }
            
            /* Pagination */
            .pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 1rem;
                margin-top: 2rem;
            }
            
            .page-link {
                padding: 0.5rem 1rem;
                background: var(--primary-color);
                color: white;
                text-decoration: none;
                border-radius: 8px;
            }
            
            .page-link:hover {
                opacity: 0.9;
            }
            
            /* Footer */
            .site-footer {
                background: var(--bg-secondary);
                border-top: 1px solid var(--border-color);
                padding: 2rem 0;
                text-align: center;
                color: var(--text-muted);
            }
            
            /* Responsive */
            @media (max-width: 768px) {
                .nav-menu {
                    display: none;
                }
                
                .content-wrapper.with-sidebar {
                    grid-template-columns: 1fr;
                }
                
                .sidebar {
                    position: static;
                }
                
                .post-header h1 {
                    font-size: 1.75rem;
                }
            }
            """;
        Files.writeString(themePath.resolve("static/css/style.css"), styleCss);

        // Create JS
        String mainJs = """
            // Default Theme JavaScript
            document.addEventListener('DOMContentLoaded', function() {
                console.log('Default theme loaded');
                
                // Dark mode toggle
                const darkModeToggle = document.getElementById('dark-mode-toggle');
                if (darkModeToggle) {
                    darkModeToggle.addEventListener('click', function() {
                        document.body.classList.toggle('dark-mode');
                        localStorage.setItem('darkMode', document.body.classList.contains('dark-mode'));
                    });
                }
                
                // Check saved dark mode preference
                if (localStorage.getItem('darkMode') === 'true') {
                    document.body.classList.add('dark-mode');
                }
            });
            """;
        Files.writeString(themePath.resolve("static/js/main.js"), mainJs);

        // Create i18n files
        String enMessages = """
            index.latestPosts=Latest Posts
            post.tags=Tags:
            post.comments=Comments
            sidebar.categories=Categories
            sidebar.tags=Tags
            sidebar.recentPosts=Recent Posts
            """;
        Files.writeString(themePath.resolve("i18n/messages_en.properties"), enMessages);

        String zhMessages = """
            index.latestPosts=最新文章
            post.tags=标签：
            post.comments=评论
            sidebar.categories=分类
            sidebar.tags=标签
            sidebar.recentPosts=最新文章
            """;
        Files.writeString(themePath.resolve("i18n/messages_zh_CN.properties"), zhMessages);

        log.info("Default theme created successfully");
    }

    private void scanAndRegisterThemes() throws IOException {
        Path themesPath = getThemesPath();
        if (!Files.exists(themesPath)) {
            return;
        }

        try (Stream<Path> paths = Files.list(themesPath)) {
            paths.filter(Files::isDirectory).forEach(themePath -> {
                try {
                    Path configPath = themePath.resolve("theme.yaml");
                    if (!Files.exists(configPath)) {
                        configPath = themePath.resolve("theme.yml");
                    }
                    if (Files.exists(configPath)) {
                        ThemeConfig config = parseThemeConfig(configPath);
                        registerTheme(config);
                    }
                } catch (IOException e) {
                    log.warn("Failed to register theme: {}", themePath.getFileName(), e);
                }
            });
        }
    }

    private void registerTheme(ThemeConfig config) {
        Theme existingTheme = themeMapper.selectOne(
            new LambdaQueryWrapper<Theme>().eq(Theme::getThemeId, config.getId())
        );
        
        if (existingTheme == null) {
            Theme theme = createThemeFromConfig(config);
            
            // Set default theme as active
            if (defaultThemeName.equals(config.getId())) {
                theme.setIsActive(true);
            }
            
            themeMapper.insert(theme);
            log.info("Registered theme: {}", config.getId());
        } else {
            // Update existing theme
            existingTheme.setName(config.getName());
            existingTheme.setVersion(config.getVersion());
            existingTheme.setDescription(config.getDescription());
            if (config.getAuthor() != null) {
                existingTheme.setAuthor(config.getAuthor().getName());
                existingTheme.setAuthorUrl(config.getAuthor().getWebsite());
            }
            try {
                existingTheme.setConfigJson(objectMapper.writeValueAsString(config));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize theme config", e);
            }
            themeMapper.updateById(existingTheme);
        }
    }

    private Theme createThemeFromConfig(ThemeConfig config) {
        Theme.ThemeBuilder builder = Theme.builder()
            .themeId(config.getId())
            .name(config.getName())
            .version(config.getVersion())
            .description(config.getDescription())
            .screenshot(config.getScreenshot())
            .status(Theme.ThemeStatus.ENABLED)
            .isActive(false);
        
        if (config.getAuthor() != null) {
            builder.author(config.getAuthor().getName());
            builder.authorUrl(config.getAuthor().getWebsite());
        }
        
        try {
            builder.configJson(objectMapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize theme config", e);
        }
        
        return builder.build();
    }

    private ThemeConfig parseThemeConfig(Path configPath) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream is = Files.newInputStream(configPath)) {
            Map<String, Object> data = yaml.load(is);
            return mapToThemeConfig(data);
        }
    }

    @SuppressWarnings("unchecked")
    private ThemeConfig mapToThemeConfig(Map<String, Object> data) {
        ThemeConfig.ThemeConfigBuilder builder = ThemeConfig.builder()
            .id((String) data.get("id"))
            .name((String) data.get("name"))
            .version((String) data.get("version"))
            .description((String) data.get("description"))
            .screenshot((String) data.get("screenshot"))
            .requires((String) data.get("requires"))
            .website((String) data.get("website"))
            .repo((String) data.get("repo"));
        
        // Parse author
        Map<String, String> authorData = (Map<String, String>) data.get("author");
        if (authorData != null) {
            builder.author(ThemeConfig.Author.builder()
                .name(authorData.get("name"))
                .website(authorData.get("website"))
                .email(authorData.get("email"))
                .build());
        }
        
        // Parse settings
        List<Map<String, Object>> settingsData = (List<Map<String, Object>>) data.get("settings");
        if (settingsData != null) {
            List<ThemeConfig.SettingGroup> settings = new ArrayList<>();
            for (Map<String, Object> groupData : settingsData) {
                ThemeConfig.SettingGroup.SettingGroupBuilder groupBuilder = ThemeConfig.SettingGroup.builder()
                    .group((String) groupData.get("group"))
                    .label((String) groupData.get("label"));
                
                List<Map<String, Object>> itemsData = (List<Map<String, Object>>) groupData.get("items");
                if (itemsData != null) {
                    List<ThemeConfig.SettingItem> items = new ArrayList<>();
                    for (Map<String, Object> itemData : itemsData) {
                        ThemeConfig.SettingItem.SettingItemBuilder itemBuilder = ThemeConfig.SettingItem.builder()
                            .name((String) itemData.get("name"))
                            .label((String) itemData.get("label"))
                            .type((String) itemData.get("type"))
                            .description((String) itemData.get("description"))
                            .defaultValue(itemData.get("defaultValue"));
                        
                        List<Map<String, String>> optionsData = (List<Map<String, String>>) itemData.get("options");
                        if (optionsData != null) {
                            List<ThemeConfig.SelectOption> options = new ArrayList<>();
                            for (Map<String, String> optionData : optionsData) {
                                options.add(ThemeConfig.SelectOption.builder()
                                    .label(optionData.get("label"))
                                    .value(optionData.get("value"))
                                    .build());
                            }
                            itemBuilder.options(options);
                        }
                        
                        items.add(itemBuilder.build());
                    }
                    groupBuilder.items(items);
                }
                
                settings.add(groupBuilder.build());
            }
            builder.settings(settings);
        }
        
        // Parse i18n
        Map<String, Object> i18nData = (Map<String, Object>) data.get("i18n");
        if (i18nData != null) {
            builder.i18n(ThemeConfig.I18nConfig.builder()
                .defaultLocale((String) i18nData.get("defaultLocale"))
                .supportedLocales((List<String>) i18nData.get("supportedLocales"))
                .build());
        }
        
        // Parse features
        Map<String, Object> featuresData = (Map<String, Object>) data.get("features");
        if (featuresData != null) {
            builder.features(ThemeConfig.Features.builder()
                .darkMode(Boolean.TRUE.equals(featuresData.get("darkMode")))
                .responsive(Boolean.TRUE.equals(featuresData.get("responsive")))
                .pwa(Boolean.TRUE.equals(featuresData.get("pwa")))
                .comments(Boolean.TRUE.equals(featuresData.get("comments")))
                .search(Boolean.TRUE.equals(featuresData.get("search")))
                .build());
        }
        
        return builder.build();
    }

    private ThemeResponse convertToResponse(Theme theme) {
        ThemeResponse.ThemeResponseBuilder builder = ThemeResponse.builder()
            .id(theme.getId())
            .themeId(theme.getThemeId())
            .name(theme.getName())
            .version(theme.getVersion())
            .author(theme.getAuthor())
            .authorUrl(theme.getAuthorUrl())
            .description(theme.getDescription())
            .screenshot(theme.getScreenshot())
            .isActive(theme.getIsActive())
            .status(theme.getStatus())
            .createdAt(theme.getCreatedAt())
            .updatedAt(theme.getUpdatedAt());
        
        // Parse config JSON
        if (theme.getConfigJson() != null) {
            try {
                ThemeConfig config = objectMapper.readValue(theme.getConfigJson(), ThemeConfig.class);
                builder.config(config);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse theme config JSON", e);
            }
        }
        
        // Parse settings JSON
        if (theme.getSettingsJson() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> settings = objectMapper.readValue(theme.getSettingsJson(), Map.class);
                builder.settings(settings);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse theme settings JSON", e);
            }
        }
        
        return builder.build();
    }

    private void unzip(Path zipFile, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = destDir.resolve(entry.getName());
                // Security check: prevent zip slip vulnerability
                if (!entryPath.normalize().startsWith(destDir.normalize())) {
                    throw new IOException("Invalid zip entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private Path findThemeYaml(Path dir) throws IOException {
        // First check root directory
        Path yamlPath = dir.resolve("theme.yaml");
        if (Files.exists(yamlPath)) {
            return yamlPath;
        }
        yamlPath = dir.resolve("theme.yml");
        if (Files.exists(yamlPath)) {
            return yamlPath;
        }
        
        // Check subdirectories (common when extracting zip)
        try (Stream<Path> paths = Files.list(dir)) {
            Optional<Path> subDir = paths.filter(Files::isDirectory).findFirst();
            if (subDir.isPresent()) {
                yamlPath = subDir.get().resolve("theme.yaml");
                if (Files.exists(yamlPath)) {
                    return yamlPath;
                }
                yamlPath = subDir.get().resolve("theme.yml");
                if (Files.exists(yamlPath)) {
                    return yamlPath;
                }
            }
        }
        
        return null;
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            Path targetPath = target.resolve(source.relativize(sourcePath));
            try {
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.warn("Failed to delete: {}", path, e);
                    }
                });
        }
    }
}
