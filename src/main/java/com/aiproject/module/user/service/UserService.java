package com.aiproject.module.user.service;

import com.aiproject.module.user.mapper.UserMapper;
import com.aiproject.module.user.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String username, String password, String email, String nickname, 
                          String description, User.UserRole role) {
        log.info("Creating user: {}", username);
        
        // Check if username exists
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email exists
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .nickname(nickname)
                .description(description)
                .role(role != null ? role : User.UserRole.SUBSCRIBER)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        userMapper.insert(user);
        return user;
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public User getUserByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public User getUserByEmail(String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    public IPage<User> listUsers(int page, int size) {
        Page<User> userPage = new Page<>(page + 1, size);
        return userMapper.selectPage(userPage, 
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
    }

    @Transactional
    public User updateUser(Long id, String nickname, String description, String avatar) {
        User user = getUserById(id);
        if (nickname != null) user.setNickname(nickname);
        if (description != null) user.setDescription(description);
        if (avatar != null) user.setAvatar(avatar);
        userMapper.updateById(user);
        return user;
    }

    @Transactional
    public User updateUserStatus(Long id, User.UserStatus status) {
        User user = getUserById(id);
        user.setStatus(status);
        userMapper.updateById(user);
        return user;
    }

    @Transactional
    public User updateUserRole(Long id, User.UserRole role) {
        User user = getUserById(id);
        user.setRole(role);
        userMapper.updateById(user);
        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userMapper.deleteById(user.getId());
    }
}
