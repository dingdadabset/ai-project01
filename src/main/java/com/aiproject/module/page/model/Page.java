package com.aiproject.module.page.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Page Entity
 * Represents a static page (About, Contact, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pages")
public class Page {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String slug;

    private String content;

    @TableField("original_content")
    private String originalContent;

    @TableField("author_id")
    private Long authorId;

    @Builder.Default
    private PageStatus status = PageStatus.DRAFT;

    @TableField("view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public enum PageStatus {
        DRAFT,      // Draft page
        PUBLISHED   // Published page
    }
}
