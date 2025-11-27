package com.aiproject.module.externaltool.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * External Tool Entity
 * Represents an external tool/website link
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("external_tools")
public class ExternalTool {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Tool name (e.g., "Notion", "Baidu")
     */
    private String name;

    /**
     * Tool description
     */
    private String description;

    /**
     * URL to the external tool
     */
    private String url;

    /**
     * Icon name or emoji (e.g., "notion", "baidu", or emoji "📝")
     */
    private String icon;

    /**
     * Icon background color (hex code)
     */
    @TableField("icon_bg_color")
    private String iconBgColor;

    /**
     * Tool category
     */
    private ToolCategory category;

    /**
     * Display order (smaller = higher priority)
     */
    @TableField("display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Whether the tool is active
     */
    @TableField("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public enum ToolCategory {
        PRODUCTIVITY,   // 生产力工具 (Notion, etc.)
        SEARCH,         // 搜索引擎 (Baidu, Google, etc.)
        SOCIAL,         // 社交媒体
        DEVELOPMENT,    // 开发工具
        ENTERTAINMENT,  // 娱乐
        FINANCE,        // 财经
        NEWS,           // 新闻
        OTHER           // 其他
    }
}
