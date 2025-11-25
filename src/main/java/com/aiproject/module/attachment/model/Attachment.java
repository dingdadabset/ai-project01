package com.aiproject.module.attachment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Attachment Entity
 * Represents an uploaded file/media
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("attachments")
public class Attachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String path;  // File path or URL

    private String url;   // Accessible URL

    @TableField("media_type")
    private String mediaType;  // MIME type

    private String suffix;  // File extension

    private Long size;  // File size in bytes

    private Integer width;  // For images

    private Integer height; // For images

    @TableField("user_id")
    private Long uploaderId;

    private AttachmentType type;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum AttachmentType {
        IMAGE,      // Image files
        VIDEO,      // Video files
        AUDIO,      // Audio files
        DOCUMENT,   // Documents (PDF, Word, etc.)
        OTHER       // Other file types
    }
}
