package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import com.xx.jaseatschoicejava.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 注册用户并对密码进行加密
     * @param user 用户对象
     * @return 注册成功返回true，否则返回false
     */
    @Override
    public boolean register(User user) {
        // 生成用户ID
        String userId = IdGenerator.toUserIdString(IdGenerator.generateId());
        user.setUserId(userId);
        // 对密码进行加密
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        return save(user);
    }

    /**
     * 用户登录，如果成功则返回JWT令牌
     * @param account 登录账号（手机号码）
     * @param password 密码
     * @return 登录成功返回JWT令牌，否则返回null
     */
    @Override
    public String login(String account, String password) {
        // 在我们的系统中，登录账号始终是手机号码
        User user = lambdaQuery()
                .eq(User::getPhone, account)
                .one();

        // 检查用户是否存在并验证密码是否正确
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 生成JWT令牌
            return jwtUtil.generateToken(user.getUserId(), user.getPhone());
        }
        return null;
    }

    /**
     * Search users by keyword
     * @param keyword Keyword to search
     * @return List of matching users
     */
    @Override
    public List<User> searchUsers(String keyword, String searchType) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of(); // Return empty list if keyword is empty
        }

        String likeKeyword = "%" + keyword.trim() + "%";
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        // 根据搜索类型进行搜索
        if ("nickname".equals(searchType)) {
            // 仅搜索昵称
            queryWrapper.like("nickname", likeKeyword);
        } else if ("phone".equals(searchType)) {
            // 仅搜索手机号
            queryWrapper.like("phone", likeKeyword);
        } else if ("email".equals(searchType)) {
            // 仅搜索邮箱
            queryWrapper.like("email", likeKeyword);
        } else {
            // 默认同时搜索昵称、手机号和邮箱
            queryWrapper.like("nickname", likeKeyword)
                    .or()
                    .like("phone", likeKeyword)
                    .or()
                    .like("email", likeKeyword);
        }

        return getBaseMapper().selectList(queryWrapper);
    }
}
