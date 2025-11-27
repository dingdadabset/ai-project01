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
 * ThemeConfig Model
 * Represents the theme.yaml configuration structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeConfig {

    /**
     * Theme identifier (folder name)
     */
    private String id;

    /**
     * Display name
     */
    private String name;

    /**
     * Theme version
     */
    private String version;

    /**
     * Theme author information
     */
    private Author author;

    /**
     * Theme description
     */
    private String description;

    /**
     * Screenshot filename or path
     */
    private String screenshot;

    /**
     * Minimum required application version
     */
    private String requires;

    /**
     * Theme website URL
     */
    private String website;

    /**
     * Theme repository URL
     */
    private String repo;

    /**
     * Configurable settings for the theme
     */
    @Builder.Default
    private List<SettingGroup> settings = new ArrayList<>();

    /**
     * i18n/localization support
     */
    @Builder.Default
    private I18nConfig i18n = new I18nConfig();

    /**
     * Theme features
     */
    @Builder.Default
    private Features features = new Features();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author {
        private String name;
        private String website;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingGroup {
        private String group;
        private String label;
        @Builder.Default
        private List<SettingItem> items = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingItem {
        private String name;
        private String label;
        private String type;  // text, textarea, switch, select, color, number
        private String description;
        private Object defaultValue;
        @Builder.Default
        private List<SelectOption> options = new ArrayList<>();
        @Builder.Default
        private Map<String, Object> validation = new HashMap<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectOption {
        private String label;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class I18nConfig {
        @Builder.Default
        private String defaultLocale = "en";
        @Builder.Default
        private List<String> supportedLocales = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Features {
        @Builder.Default
        private boolean darkMode = true;
        @Builder.Default
        private boolean responsive = true;
        @Builder.Default
        private boolean pwa = false;
        @Builder.Default
        private boolean comments = true;
        @Builder.Default
        private boolean search = true;
    }
}
