package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.User;

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
}
