package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseResult<?> register(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return ResponseResult.success("注册成功");
        }
        return ResponseResult.fail("500", "注册失败");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseResult<?> login(@RequestBody User loginUser) {
        String token = userService.login(loginUser.getPhone(), loginUser.getPassword());
        if (token != null) {
            return ResponseResult.success(token);
        }
        return ResponseResult.fail("500", "手机号或密码错误");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public ResponseResult<?> getUserInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            return ResponseResult.success(user);
        }
        return ResponseResult.fail("404", "用户不存在");
    }
}
