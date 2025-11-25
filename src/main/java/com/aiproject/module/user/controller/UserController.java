package com.aiproject.module.user.controller;

import com.aiproject.module.user.model.User;
import com.aiproject.module.user.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User Controller
 * REST API endpoints for users
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> request) {
        log.info("POST /api/users - Creating new user");
        User.UserRole role = request.get("role") != null ? 
                User.UserRole.valueOf(request.get("role")) : User.UserRole.SUBSCRIBER;
        User user = userService.createUser(
                request.get("username"),
                request.get("password"),
                request.get("email"),
                request.get("nickname"),
                request.get("description"),
                role
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{}", id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        log.info("GET /api/users/username/{}", username);
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    /**
     * List all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /api/users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * List users with pagination
     */
    @GetMapping("/page")
    public ResponseEntity<IPage<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/users/page - page: {}, size: {}", page, size);
        IPage<User> users = userService.listUsers(page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Update a user
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/users/{}", id);
        User user = userService.updateUser(id, 
                request.get("nickname"),
                request.get("description"),
                request.get("avatar"));
        return ResponseEntity.ok(user);
    }

    /**
     * Update user status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/users/{}/status", id);
        User.UserStatus status = User.UserStatus.valueOf(request.get("status"));
        User user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user role
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/users/{}/role", id);
        User.UserRole role = User.UserRole.valueOf(request.get("role"));
        User user = userService.updateUserRole(id, role);
        return ResponseEntity.ok(user);
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
