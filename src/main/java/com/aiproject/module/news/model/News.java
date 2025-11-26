package com.aiproject.module.news.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * News Entity
 * Represents a hot news item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("news")
public class News {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String content;

    private String source;

    @TableField("source_url")
    private String sourceUrl;

    private String thumbnail;

    private NewsCategory category;

    @TableField("view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @TableField("is_hot")
    @Builder.Default
    private Boolean isHot = false;

    @TableField("hot_score")
    @Builder.Default
    private Integer hotScore = 0;

    @TableField(value = "published_at")
    private LocalDateTime publishedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public enum NewsCategory {
        TECHNOLOGY,  // 科技
        FINANCE,     // 财经
        POLITICS,    // 政治
        SPORTS,      // 体育
        ENTERTAINMENT, // 娱乐
        HEALTH,      // 健康
        SCIENCE,     // 科学
        WORLD,       // 国际
        DOMESTIC,    // 国内
        OTHER        // 其他
    }
}
