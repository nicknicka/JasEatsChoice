package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.User;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * Register user
     * @param user User object
     * @return true if successful, false otherwise
     */
    boolean register(User user);

    /**
     * User login
     * @param phone Phone number
     * @param password Password
     * @return JWT token if successful, null otherwise
     */
    String login(String phone, String password);

    /**
     * Search users by keyword
     * @param keyword Keyword to search
     * @param searchType Search type: nickname, phone, email; if null or empty, search all three fields
     * @return List of matching users
     */
    List<User> searchUsers(String keyword, String searchType);

    /**
     * 检查邮箱是否已被注册
     * @param email 邮箱地址
     * @return 如果邮箱已存在返回true，否则返回false
     */
    boolean isEmailExists(String email);

    /**
     * 检查手机号是否已被注册
     * @param phone 手机号
     * @return 如果手机号已存在返回true，否则返回false
     */
    boolean isPhoneExists(String phone);
}
