package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.service.AliyunSMSService;
import com.xx.jaseatschoicejava.service.VerificationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码Service实现
 */
@Service
public class VerificationServiceImpl implements VerificationService {

    @Resource
    private AliyunSMSService aliyunSMSService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String CODE_PREFIX = "verification_code:";
    private static final int CODE_EXPIRE_MINUTES = 5; // 验证码5分钟过期

    @Override
    public boolean sendVerificationCode(String phone, String type) {
        try {
            // 生成6位随机验证码
            String code = generateCode();

            // 存储到Redis，key为: verification_code:phone:type
            String redisKey = CODE_PREFIX + phone + ":" + type;
            stringRedisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 发送短信
            aliyunSMSService.sendSmsVerifyCode(phone, code);

            System.out.println("验证码已发送：手机号=" + phone + "，验证码=" + code + "，类型=" + type);
            return true;
        } catch (Exception e) {
            System.err.println("发送验证码失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean verifyCode(String phone, String code, String type) {
        String redisKey = CODE_PREFIX + phone + ":" + type;
        String savedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (savedCode == null) {
            System.out.println("验证码不存在或已过期：手机号=" + phone + "，类型=" + type);
            return false;
        }

        boolean valid = savedCode.equals(code);

        if (valid) {
            // 验证成功后删除验证码
            stringRedisTemplate.delete(redisKey);
            System.out.println("验证码验证成功：手机号=" + phone);
        } else {
            System.out.println("验证码错误：手机号=" + phone + "，期望=" + savedCode + "，实际=" + code);
        }

        return valid;
    }

    @Override
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
