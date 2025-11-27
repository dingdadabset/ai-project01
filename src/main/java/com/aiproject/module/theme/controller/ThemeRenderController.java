package com.aiproject.module.theme.controller;

import com.aiproject.module.category.service.CategoryService;
import com.aiproject.module.post.model.PostResponse;
import com.aiproject.module.post.service.PostService;
import com.aiproject.module.tag.service.TagService;
import com.aiproject.module.theme.model.ThemeContext;
import com.aiproject.module.theme.model.ThemeResponse;
import com.aiproject.module.theme.service.ThemeService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import jakarta.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Theme Render Controller
 * Handles server-side rendering of theme templates
 */
@Slf4j
@Controller
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeRenderController {

    private final ThemeService themeService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final ObjectMapper objectMapper;

    @Value("${theme.directory:themes}")
    private String themeDirectory;

    @Value("${site.title:My Blog}")
    private String siteTitle;

    @Value("${site.url:}")
    private String siteUrl;

    @Value("${site.description:}")
    private String siteDescription;

    private TemplateEngine themeTemplateEngine;

    @PostConstruct
    public void init() {
        // Configure Thymeleaf for theme templates
        themeTemplateEngine = new TemplateEngine();
        
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(getThemesPath().toString() + "/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCacheable(false); // Disable cache for development
        resolver.setCharacterEncoding("UTF-8");
        
        themeTemplateEngine.setTemplateResolver(resolver);
    }

    /**
     * Render theme index page
     */
    @GetMapping("/{themeId}")
    public String renderIndex(
            @PathVariable String themeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "en") String lang,
            Model model) {
        
        log.info("Rendering theme index: {}", themeId);
        
        try {
            ThemeResponse theme = themeService.getThemeByThemeId(themeId);
            ThemeContext context = buildContext(theme, lang);
            
            // Add posts
            IPage<PostResponse> postsPage = postService.listPublishedPosts(page, size);
            context.setPosts(postsPage.getRecords().stream()
                .map(this::convertToPostInfo)
                .collect(Collectors.toList()));
            
            // Add pagination
            String previousUrl = UriComponentsBuilder.fromPath("/themes/{themeId}")
                .queryParam("page", page - 1)
                .queryParam("size", size)
                .buildAndExpand(themeId)
                .toUriString();
            String nextUrl = UriComponentsBuilder.fromPath("/themes/{themeId}")
                .queryParam("page", page + 1)
                .queryParam("size", size)
                .buildAndExpand(themeId)
                .toUriString();
            
            context.setPagination(ThemeContext.Pagination.builder()
                .current(page)
                .size(size)
                .total(postsPage.getTotal())
                .pages((int) postsPage.getPages())
                .hasPrevious(page > 0)
                .hasNext(page < postsPage.getPages() - 1)
                .previousUrl(previousUrl)
                .nextUrl(nextUrl)
                .build());
            
            populateModel(model, context, theme);
            model.addAttribute("template", "index");
            
            return themeId + "/templates/layout";
            
        } catch (Exception e) {
            log.error("Failed to render theme index", e);
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Render theme post page
     */
    @GetMapping("/{themeId}/posts/{slug}")
    public String renderPost(
            @PathVariable String themeId,
            @PathVariable String slug,
            @RequestParam(defaultValue = "en") String lang,
            Model model) {
        
        log.info("Rendering theme post: {} - {}", themeId, slug);
        
        try {
            ThemeResponse theme = themeService.getThemeByThemeId(themeId);
            ThemeContext context = buildContext(theme, lang);
            
            // Get post
            PostResponse post = postService.getPostBySlug(slug);
            context.setPost(convertToPostInfo(post));
            
            populateModel(model, context, theme);
            model.addAttribute("template", "post");
            
            return themeId + "/templates/layout";
            
        } catch (Exception e) {
            log.error("Failed to render theme post", e);
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Preview theme without activating
     * Returns a simple HTML preview that showcases the theme's style and settings
     */
    @GetMapping(value = "/{themeId}/preview", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String previewTheme(
            @PathVariable String themeId,
            @RequestParam(defaultValue = "en") String lang) {
        
        log.info("Previewing theme: {}", themeId);
        
        try {
            ThemeResponse theme = themeService.getThemeByThemeId(themeId);
            
            // Get theme settings with defaults
            Map<String, Object> settings = theme.getSettings() != null ? theme.getSettings() : getDefaultSettings(theme);
            
            String siteName = settings.getOrDefault("siteName", "Anime Blog").toString();
            String siteDescription = settings.getOrDefault("siteDescription", "A beautiful anime-style personal blog").toString();
            String currentBackground = settings.getOrDefault("currentBackground", "bg1").toString();
            String primaryColor = settings.getOrDefault("primaryColor", "#ff69b4").toString();
            String accentColor = settings.getOrDefault("accentColor", "#ff1493").toString();
            
            // Get sample posts for preview
            IPage<PostResponse> postsPage = postService.listPublishedPosts(0, 5);
            List<ThemeContext.PostInfo> posts = postsPage.getRecords().stream()
                .map(this::convertToPostInfo)
                .collect(Collectors.toList());
            
            // Build HTML preview
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"").append(lang).append("\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("    <title>").append(escapeHtml(siteName)).append(" - Preview</title>\n");
            html.append("    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n");
            html.append("    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n");
            html.append("    <link href=\"https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700&display=swap\" rel=\"stylesheet\">\n");
            html.append("    <link rel=\"stylesheet\" href=\"/themes/").append(themeId).append("/static/css/style.css\">\n");
            html.append("    <style>\n");
            html.append("        :root {\n");
            html.append("            --primary-color: ").append(primaryColor).append(";\n");
            html.append("            --accent-color: ").append(accentColor).append(";\n");
            html.append("            --text-color: #ffffff;\n");
            html.append("        }\n");
            html.append("    </style>\n");
            html.append("    <meta name=\"theme-color\" content=\"").append(primaryColor).append("\">\n");
            html.append("</head>\n");
            html.append("<body class=\"background-").append(currentBackground).append("\">\n");
            html.append("    <header class=\"site-header\">\n");
            html.append("        <nav class=\"navbar\">\n");
            html.append("            <a href=\"/\" class=\"logo\">").append(escapeHtml(siteName)).append("</a>\n");
            html.append("            <ul class=\"nav-menu\">\n");
            html.append("                <li><a href=\"/\">Home</a></li>\n");
            html.append("                <li><a href=\"/posts\">Posts</a></li>\n");
            html.append("                <li><a href=\"/categories\">Categories</a></li>\n");
            html.append("                <li><a href=\"/tags\">Tags</a></li>\n");
            html.append("                <li><a href=\"/about\">About</a></li>\n");
            html.append("            </ul>\n");
            html.append("        </nav>\n");
            html.append("    </header>\n");
            html.append("    <main class=\"main-content\">\n");
            html.append("        <div class=\"container\">\n");
            html.append("            <div class=\"content-wrapper with-sidebar\">\n");
            html.append("                <div class=\"content\">\n");
            html.append("                    <section class=\"posts-section\">\n");
            html.append("                        <h1 class=\"section-title\">✨ Latest Posts</h1>\n");
            html.append("                        <div class=\"posts-grid\">\n");
            
            if (posts.isEmpty()) {
                html.append("                            <div class=\"no-posts\"><p>No posts yet. Start creating! ✨</p></div>\n");
            } else {
                for (ThemeContext.PostInfo post : posts) {
                    html.append("                            <article class=\"post-card\">\n");
                    if (post.getThumbnail() != null && !post.getThumbnail().isEmpty()) {
                        html.append("                                <img src=\"").append(escapeHtml(post.getThumbnail())).append("\" alt=\"\" class=\"post-thumbnail\">\n");
                    }
                    html.append("                                <div class=\"post-content\">\n");
                    html.append("                                    <h2><a href=\"#\">").append(escapeHtml(post.getTitle())).append("</a></h2>\n");
                    html.append("                                    <p class=\"post-meta\">\n");
                    html.append("                                        <span>📝 ").append(escapeHtml(post.getAuthorName() != null ? post.getAuthorName() : "Author")).append("</span>\n");
                    html.append("                                        <span>📅 ").append(post.getPublishedAt() != null ? post.getPublishedAt().substring(0, 10) : "Date").append("</span>\n");
                    if (post.getViewCount() != null && post.getViewCount() > 0) {
                        html.append("                                        <span>👀 ").append(post.getViewCount()).append("</span>\n");
                    }
                    html.append("                                    </p>\n");
                    if (post.getSummary() != null) {
                        html.append("                                    <p class=\"post-summary\">").append(escapeHtml(post.getSummary())).append("</p>\n");
                    }
                    html.append("                                </div>\n");
                    html.append("                            </article>\n");
                }
            }
            
            html.append("                        </div>\n");
            html.append("                    </section>\n");
            html.append("                </div>\n");
            html.append("                <aside class=\"sidebar\">\n");
            html.append("                    <div class=\"widget bg-info-widget\">\n");
            html.append("                        <h3>🎨 Theme Preview</h3>\n");
            html.append("                        <p style=\"font-size: 0.875rem; opacity: 0.8;\">This is a preview of the ").append(escapeHtml(theme.getName())).append(" theme.</p>\n");
            html.append("                        <p style=\"font-size: 0.875rem; opacity: 0.8;\">Background: ").append(escapeHtml(currentBackground)).append("</p>\n");
            html.append("                    </div>\n");
            html.append("                </aside>\n");
            html.append("            </div>\n");
            html.append("        </div>\n");
            html.append("    </main>\n");
            html.append("    <footer class=\"site-footer\">\n");
            html.append("        <div class=\"container\">\n");
            html.append("            <p>© 2025 ").append(escapeHtml(siteName)).append("</p>\n");
            html.append("            <p>Powered by ").append(escapeHtml(theme.getName())).append(" ✨</p>\n");
            html.append("        </div>\n");
            html.append("    </footer>\n");
            html.append("    <script src=\"/themes/").append(themeId).append("/static/js/main.js\"></script>\n");
            html.append("</body>\n");
            html.append("</html>");
            
            return html.toString();
            
        } catch (Exception e) {
            log.error("Failed to preview theme", e);
            return "<html><body><h1>Preview Error</h1><p>" + escapeHtml(e.getMessage()) + "</p></body></html>";
        }
    }
    
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /**
     * Render custom template
     */
    public String renderTemplate(String themeId, String templateName, ThemeContext context) {
        Context thymeleafContext = new Context(Locale.getDefault());
        
        // Add all context data
        thymeleafContext.setVariable("site", context.getSite());
        thymeleafContext.setVariable("user", context.getUser());
        thymeleafContext.setVariable("post", context.getPost());
        thymeleafContext.setVariable("posts", context.getPosts());
        thymeleafContext.setVariable("categories", context.getCategories());
        thymeleafContext.setVariable("tags", context.getTags());
        thymeleafContext.setVariable("page", context.getPage());
        thymeleafContext.setVariable("settings", context.getSettings());
        thymeleafContext.setVariable("config", context.getConfig());
        thymeleafContext.setVariable("pagination", context.getPagination());
        thymeleafContext.setVariable("locale", context.getLocale());
        
        return themeTemplateEngine.process(themeId + "/templates/" + templateName, thymeleafContext);
    }

    // ==================== Private Helper Methods ====================

    private Path getThemesPath() {
        return Paths.get(System.getProperty("user.dir"), themeDirectory);
    }

    private ThemeContext buildContext(ThemeResponse theme, String locale) {
        ThemeContext.ThemeContextBuilder builder = ThemeContext.builder()
            .locale(locale);
        
        // Site info
        builder.site(ThemeContext.SiteInfo.builder()
            .title(siteTitle)
            .url(siteUrl)
            .description(siteDescription)
            .build());
        
        // Categories
        try {
            builder.categories(categoryService.listCategories().stream()
                .map(cat -> ThemeContext.CategoryInfo.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .slug(cat.getSlug())
                    .description(cat.getDescription())
                    .postCount(cat.getPostCount())
                    .build())
                .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to load categories", e);
        }
        
        // Tags
        try {
            builder.tags(tagService.listTags().stream()
                .map(tag -> ThemeContext.TagInfo.builder()
                    .id(tag.getId())
                    .name(tag.getName())
                    .slug(tag.getSlug())
                    .postCount(tag.getPostCount())
                    .build())
                .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to load tags", e);
        }
        
        // Theme settings
        if (theme.getSettings() != null) {
            builder.settings(theme.getSettings());
        }
        
        return builder.build();
    }

    private void populateModel(Model model, ThemeContext context, ThemeResponse theme) {
        model.addAttribute("site", context.getSite());
        model.addAttribute("user", context.getUser());
        model.addAttribute("post", context.getPost());
        model.addAttribute("posts", context.getPosts());
        model.addAttribute("categories", context.getCategories());
        model.addAttribute("tags", context.getTags());
        model.addAttribute("page", context.getPage());
        model.addAttribute("settings", context.getSettings() != null ? context.getSettings() : getDefaultSettings(theme));
        model.addAttribute("config", context.getConfig());
        model.addAttribute("pagination", context.getPagination());
        model.addAttribute("locale", context.getLocale());
        model.addAttribute("theme", theme);
        model.addAttribute("pageTitle", "Home");
        
        // Add recent posts for sidebar
        try {
            IPage<PostResponse> recentPosts = postService.listPublishedPosts(0, 5);
            model.addAttribute("recentPosts", recentPosts.getRecords().stream()
                .map(this::convertToPostInfo)
                .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to load recent posts", e);
        }
    }

    private Map<String, Object> getDefaultSettings(ThemeResponse theme) {
        if (theme.getConfig() != null && theme.getConfig().getSettings() != null) {
            return theme.getConfig().getSettings().stream()
                .flatMap(group -> group.getItems().stream())
                .collect(Collectors.toMap(
                    item -> item.getName(),
                    item -> item.getDefaultValue() != null ? item.getDefaultValue() : "",
                    (a, b) -> a
                ));
        }
        return Map.of();
    }

    private ThemeContext.PostInfo convertToPostInfo(PostResponse post) {
        return ThemeContext.PostInfo.builder()
            .id(post.getId())
            .title(post.getTitle())
            .slug(post.getSlug())
            .summary(post.getSummary())
            .content(post.getContent())
            .thumbnail(post.getThumbnail())
            .status(post.getStatus() != null ? post.getStatus().name() : null)
            .authorName(post.getAuthorName())
            .categoryName(post.getCategoryName())
            .tags(post.getTags() != null ? List.copyOf(post.getTags()) : null)
            .viewCount(post.getViewCount())
            .likeCount(post.getLikeCount())
            .commentCount(post.getCommentCount())
            .allowComment(post.getAllowComment())
            .publishedAt(post.getPublishedAt() != null ? post.getPublishedAt().toString() : null)
            .createdAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : null)
            .updatedAt(post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null)
            .build();
    }
}
