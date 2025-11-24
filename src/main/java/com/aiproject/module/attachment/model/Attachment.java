package com.aiproject.module.attachment.model;

import com.aiproject.module.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Attachment Entity
 * Represents an uploaded file/media
 */
@Entity
@Table(name = "attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 500)
    private String path;  // File path or URL

    @Column(nullable = false, length = 500)
    private String url;   // Accessible URL

    @Column(length = 100)
    private String mediaType;  // MIME type

    @Column(length = 10)
    private String suffix;  // File extension

    @Column(nullable = false)
    private Long size;  // File size in bytes

    @Column(nullable = false)
    private Integer width;  // For images

    @Column(nullable = false)
    private Integer height; // For images

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User uploader;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentType type;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum AttachmentType {
        IMAGE,      // Image files
        VIDEO,      // Video files
        AUDIO,      // Audio files
        DOCUMENT,   // Documents (PDF, Word, etc.)
        OTHER       // Other file types
    }
}
