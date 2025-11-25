package com.aiproject.module.post.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Post Entity
 * Represents a blog post/article
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("posts")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String content;

    @TableField("original_content")
    private String originalContent;  // For Markdown source

    private String thumbnail;

    private PostStatus status;

    @TableField("author_id")
    private Long authorId;

    @TableField("category_id")
    private Long categoryId;

    @TableField("view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @TableField("like_count")
    @Builder.Default
    private Long likeCount = 0L;

    @TableField("comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    @TableField("top_priority")
    @Builder.Default
    private Integer topPriority = 0;  // 0 = normal, higher = pinned

    @TableField("allow_comment")
    @Builder.Default
    private Boolean allowComment = true;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public enum PostStatus {
        DRAFT,      // Draft post, not published
        PUBLISHED,  // Published and visible
        PRIVATE,    // Private, only visible to author
        SCHEDULED   // Scheduled for future publishing
    }
}
