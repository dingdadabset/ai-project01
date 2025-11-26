package com.aiproject.module.auth.service;

import com.aiproject.module.auth.model.AuthResponse;
import com.aiproject.module.auth.model.LoginRequest;
import com.aiproject.module.auth.model.RegisterRequest;
import com.aiproject.module.user.mapper.UserMapper;
import com.aiproject.module.user.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.UUID;

/**
 * Authentication Service
 * Handles user authentication and registration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate user with username and password
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        
        if (user == null) {
            throw new RuntimeException("Invalid username or password");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }
        
        String token = generateToken(user);
        return AuthResponse.fromUser(user, token);
    }

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());
        
        // Check if username exists
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())) > 0) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email exists
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())) > 0) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .role(User.UserRole.SUBSCRIBER)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        userMapper.insert(user);
        
        String token = generateToken(user);
        return AuthResponse.fromUser(user, token);
    }

    /**
     * Get current user by token
     */
    public AuthResponse.UserDto getCurrentUser(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token is required");
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Long userId = validateToken(token);
        if (userId == null) {
            throw new RuntimeException("Invalid token");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .description(user.getDescription())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .build();
    }

    /**
     * Generate a simple token (for development purposes)
     * In production, use JWT or other secure token mechanism
     */
    private String generateToken(User user) {
        String tokenData = user.getId() + ":" + UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    /**
     * Validate token and extract user ID
     */
    private Long validateToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length >= 1) {
                return Long.parseLong(parts[0]);
            }
        } catch (Exception e) {
            log.error("Token validation failed", e);
        }
        return null;
    }
}
