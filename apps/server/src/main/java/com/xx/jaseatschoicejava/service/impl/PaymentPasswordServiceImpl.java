package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.constants.VerificationType;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.service.PaymentPasswordService;
import com.xx.jaseatschoicejava.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付密码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPasswordServiceImpl implements PaymentPasswordService {

    private final UserMapper userMapper;
    private final VerificationService verificationService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setupPaymentPassword(String userId, String password) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否已设置支付密码
        if (Boolean.TRUE.equals(user.getHasPaymentPassword())) {
            throw new RuntimeException("已设置支付密码，如需修改请使用修改功能");
        }

        // 加密密码
        String encryptedPassword = passwordEncoder.encode(password);

        // 更新用户信息
        user.setPaymentPassword(encryptedPassword);
        user.setHasPaymentPassword(true);

        int updated = userMapper.updateById(user);
        if (updated == 0) {
            throw new RuntimeException("设置支付密码失败");
        }

        log.info("用户{}成功设置支付密码", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePaymentPassword(String userId, String oldPassword, String newPassword) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否已设置支付密码
        if (Boolean.FALSE.equals(user.getHasPaymentPassword()) || user.getPaymentPassword() == null) {
            throw new RuntimeException("未设置支付密码，请先设置");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPaymentPassword())) {
            throw new RuntimeException("旧支付密码不正确");
        }

        // 加密新密码
        String encryptedNewPassword = passwordEncoder.encode(newPassword);

        // 更新用户信息
        user.setPaymentPassword(encryptedNewPassword);

        int updated = userMapper.updateById(user);
        if (updated == 0) {
            throw new RuntimeException("修改支付密码失败");
        }

        log.info("用户{}成功修改支付密码", userId);
    }

    @Override
    public boolean verifyPaymentPassword(String userId, String password) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否已设置支付密码
        if (Boolean.FALSE.equals(user.getHasPaymentPassword()) || user.getPaymentPassword() == null) {
            return false;
        }

        // 验证密码
        return passwordEncoder.matches(password, user.getPaymentPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPaymentPassword(String userId, String newPassword, String verificationCode) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 校验短信验证码：复用 VerificationService 的 payment 类型验证码存储
        // （前端「通过手机验证码重置」发送的正是该类型），校验通过后由它删除已使用的验证码。
        if (verificationCode == null || verificationCode.isBlank()) {
            throw new IllegalArgumentException("短信验证码不能为空");
        }
        if (user.getPhone() == null || user.getPhone().isBlank()
                || !verificationService.verifyCode(user.getPhone(), verificationCode, VerificationType.PAYMENT)) {
            log.warn("用户{}重置支付密码失败：短信验证码校验未通过", userId);
            throw new IllegalArgumentException("短信验证码错误或已过期");
        }

        // 加密新密码
        String encryptedNewPassword = passwordEncoder.encode(newPassword);

        // 更新用户信息
        user.setPaymentPassword(encryptedNewPassword);
        user.setHasPaymentPassword(true);

        int updated = userMapper.updateById(user);
        if (updated == 0) {
            throw new RuntimeException("重置支付密码失败");
        }

        log.info("用户{}成功重置支付密码", userId);
    }

    @Override
    public boolean hasPaymentPassword(String userId) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            return false;
        }

        return Boolean.TRUE.equals(user.getHasPaymentPassword());
    }
}
