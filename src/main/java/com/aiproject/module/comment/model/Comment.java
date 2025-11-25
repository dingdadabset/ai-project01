package com.aiproject.module.comment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Comment Entity
 * Represents a comment on a blog post
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("user_id")
    private Long userId;

    // For guest comments (if user is null)
    @TableField("guest_name")
    private String guestName;

    @TableField("guest_email")
    private String guestEmail;

    private String content;

    @TableField("parent_id")
    private Long parentId;  // For nested comments

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @Builder.Default
    private CommentStatus status = CommentStatus.PENDING;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum CommentStatus {
        PENDING,    // Awaiting moderation
        APPROVED,   // Approved and visible
        SPAM,       // Marked as spam
        DELETED     // Soft deleted
    }
}
