package com.aiproject.module.auth.model;

import com.aiproject.module.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private UserDto user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String description;
        private String role;
        private String status;
    }

    public static AuthResponse fromUser(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .user(UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .description(user.getDescription())
                        .role(user.getRole() != null ? user.getRole().name() : null)
                        .status(user.getStatus() != null ? user.getStatus().name() : null)
                        .build())
                .build();
    }
}
