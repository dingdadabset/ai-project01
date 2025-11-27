package com.aiproject.module.theme.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Theme Entity
 * Represents a theme in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("themes")
public class Theme {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Theme unique identifier (folder name)
     */
    private String themeId;

    /**
     * Display name
     */
    private String name;

    /**
     * Theme version
     */
    private String version;

    /**
     * Theme author
     */
    private String author;

    /**
     * Author website URL
     */
    private String authorUrl;

    /**
     * Theme description
     */
    private String description;

    /**
     * Screenshot URL/path
     */
    private String screenshot;

    /**
     * Theme configuration in JSON format
     */
    @TableField("config_json")
    private String configJson;

    /**
     * User-customized settings in JSON format
     */
    @TableField("settings_json")
    private String settingsJson;

    /**
     * Whether this theme is currently active
     */
    @TableField("is_active")
    @Builder.Default
    private Boolean isActive = false;

    /**
     * Theme status
     */
    private ThemeStatus status;

    /**
     * Theme template engine type
     */
    @TableField("template_engine")
    @Builder.Default
    private String templateEngine = "thymeleaf";

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public enum ThemeStatus {
        ENABLED,    // Theme is enabled and can be activated
        DISABLED,   // Theme is disabled
        ERROR       // Theme has configuration errors
    }
}
