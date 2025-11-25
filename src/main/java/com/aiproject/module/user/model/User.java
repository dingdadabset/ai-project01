package com.aiproject.module.user.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Entity
 * Represents a blog user (author, admin, or commenter)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String nickname;

    private String avatar;

    private String description;

    private UserRole role;

    private UserStatus status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public enum UserRole {
        ADMIN,      // Administrator with full access
        AUTHOR,     // Can create and manage own posts
        SUBSCRIBER  // Can only comment and view posts
    }

    public enum UserStatus {
        ACTIVE,     // Active user
        INACTIVE,   // Inactive user
        BANNED      // Banned user
    }
}
