package com.aiproject.module.theme.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ThemeContext Model
 * Standardized context variables for theme templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeContext {

    /**
     * Site information
     */
    @Builder.Default
    private SiteInfo site = new SiteInfo();

    /**
     * Current user information (if logged in)
     */
    private UserInfo user;

    /**
     * Current post (for single post pages)
     */
    private PostInfo post;

    /**
     * List of posts (for list pages)
     */
    @Builder.Default
    private List<PostInfo> posts = new ArrayList<>();

    /**
     * Categories list
     */
    @Builder.Default
    private List<CategoryInfo> categories = new ArrayList<>();

    /**
     * Tags list
     */
    @Builder.Default
    private List<TagInfo> tags = new ArrayList<>();

    /**
     * Current page (for static pages)
     */
    private PageInfo page;

    /**
     * Theme settings (user-customized)
     */
    @Builder.Default
    private Map<String, Object> settings = new HashMap<>();

    /**
     * General configuration
     */
    @Builder.Default
    private Map<String, Object> config = new HashMap<>();

    /**
     * Pagination information
     */
    private Pagination pagination;

    /**
     * Current locale for i18n
     */
    @Builder.Default
    private String locale = "en";

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SiteInfo {
        @Builder.Default
        private String title = "My Blog";
        @Builder.Default
        private String url = "";
        @Builder.Default
        private String description = "";
        @Builder.Default
        private String logo = "";
        @Builder.Default
        private String favicon = "";
        @Builder.Default
        private String copyright = "";
        @Builder.Default
        private Map<String, String> social = new HashMap<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String email;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostInfo {
        private Long id;
        private String title;
        private String slug;
        private String summary;
        private String content;
        private String thumbnail;
        private String status;
        private String authorName;
        private String authorAvatar;
        private String categoryName;
        private Long categoryId;
        private List<String> tags;
        private Long viewCount;
        private Long likeCount;
        private Integer commentCount;
        private Boolean allowComment;
        private String publishedAt;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private Integer postCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagInfo {
        private Long id;
        private String name;
        private String slug;
        private Integer postCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private Long id;
        private String title;
        private String slug;
        private String content;
        private String status;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private int current;
        private int size;
        private long total;
        private int pages;
        private boolean hasPrevious;
        private boolean hasNext;
        private String previousUrl;
        private String nextUrl;
    }
}
