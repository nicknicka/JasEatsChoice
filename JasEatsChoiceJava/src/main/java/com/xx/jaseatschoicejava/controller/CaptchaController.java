package com.xx.jaseatschoicejava.controller;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import com.xx.jaseatschoicejava.common.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.hutool.core.math.Calculator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/captcha")
@Api(tags = "验证码接口")
public class CaptchaController {

    @Value("${captcha.test.fixed-code-enabled:false}")
    private boolean fixedCodeEnabled;

    @Value("${captcha.test.fixed-code:8888}")
    private String fixedCode;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 生成算术验证码
     */
    @GetMapping("/checkCode")
    @ApiOperation("生成算术验证码")
    public ResponseResult<?> generateCaptcha() {
        try {
            // 创建图形验证码实例，设置宽、高、字符数和干扰线数
            LineCaptcha captcha = new LineCaptcha(150, 50, 3, 7);

            // 设置验证码生成器为算术生成器
            MathGenerator mathGenerator = new MathGenerator();
            captcha.setGenerator(mathGenerator);

            // 获取验证码表达式
            String captchaExpression = captcha.getCode(); // 如 "1 +40="
            log.info("验证码表达式: " + captchaExpression);

            // 计算表达式结果
            String captchaResult = String.valueOf((int) Calculator.conversion(captchaExpression));
            log.info("验证码结果: " + captchaResult);

            // 获取验证码图片的Base64编码（包含data:image/png;base64,前缀）
            String captchaBase64 = captcha.getImageBase64();

            // 生成唯一key用于验证
            String checkCodeKey = UUID.randomUUID().toString().replace("-", "");

            String effectiveCaptcha = fixedCodeEnabled ? fixedCode : captchaResult;

            // 将验证码结果存入Redis，有效期5分钟
            redisTemplate.opsForValue().set("captcha:" + checkCodeKey, effectiveCaptcha, 5, TimeUnit.MINUTES);

            // 构造返回结果
            Map<String, String> result = new HashMap<>();
            result.put("checkCode", captchaBase64);
            result.put("checkCodeKey", checkCodeKey);
            // 返回验证码答案，仅用于开发环境或测试环境快速验证
            result.put("captchaAnswer", effectiveCaptcha);
            result.put("fixedCaptchaEnabled", String.valueOf(fixedCodeEnabled));
            if (fixedCodeEnabled) {
                result.put("fixedCaptchaCode", fixedCode);
                log.warn("验证码固定测试模式已启用，当前固定验证码: {}", fixedCode);
            }

            return ResponseResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "生成验证码失败: " + e.getMessage());
        }
    }
}
