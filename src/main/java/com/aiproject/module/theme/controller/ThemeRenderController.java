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
     */
    @GetMapping("/{themeId}/preview")
    public String previewTheme(
            @PathVariable String themeId,
            @RequestParam(defaultValue = "en") String lang,
            Model model) {
        
        log.info("Previewing theme: {}", themeId);
        
        try {
            ThemeResponse theme = themeService.getThemeByThemeId(themeId);
            ThemeContext context = buildContext(theme, lang);
            
            // Add sample posts for preview
            IPage<PostResponse> postsPage = postService.listPublishedPosts(0, 5);
            context.setPosts(postsPage.getRecords().stream()
                .map(this::convertToPostInfo)
                .collect(Collectors.toList()));
            
            context.setPagination(ThemeContext.Pagination.builder()
                .current(0)
                .size(10)
                .total(postsPage.getTotal())
                .pages((int) postsPage.getPages())
                .hasPrevious(false)
                .hasNext(postsPage.getPages() > 1)
                .build());
            
            populateModel(model, context, theme);
            model.addAttribute("template", "index");
            model.addAttribute("isPreview", true);
            
            return themeId + "/templates/layout";
            
        } catch (Exception e) {
            log.error("Failed to preview theme", e);
            model.addAttribute("error", e.getMessage());
            return "error";
        }
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
