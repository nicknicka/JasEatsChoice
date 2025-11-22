package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Register user with password encryption
     * @param user User object
     * @return true if successful, false otherwise
     */
    @Override
    public boolean register(User user) {
        // Encrypt the password
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        return save(user);
    }

    /**
     * User login, return JWT token if successful
     * @param phone Phone number
     * @param password Password
     * @return JWT token if successful, null otherwise
     */
    @Override
public String login(String phone, String password) {
        // Find user by phone
        User user = lambdaQuery()
                .eq(User::getPhone, phone)
                .one();

        // Check if user exists and password is correct
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // Generate JWT token
            return JwtUtil.generateToken(user.getId(), user.getPhone());
        }
        return null;
    }
}
