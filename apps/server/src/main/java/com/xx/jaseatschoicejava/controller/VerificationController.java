package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.constants.VerificationType;
import com.xx.jaseatschoicejava.service.VerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证码Controller
 */
@Api(tags = "验证码管理")
@RestController
@RequestMapping("/v1/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @ApiOperation("发送验证码")
    @PostMapping("/send")
    public ResponseResult<Map<String, Object>> sendVerificationCode(
            @ApiParam("手机号") @RequestParam String phone,
            @ApiParam("验证码类型: register-注册, login-登录, reset-重置密码, payment-支付密码")
            @RequestParam(defaultValue = VerificationType.REGISTER) String type) {

        boolean success = verificationService.sendVerificationCode(phone, type);

        Map<String, Object> result = new HashMap<>();
        result.put("phone", phone);
        result.put("type", type);
        result.put("expireTime", 300); // 5分钟
        result.put("note", "验证码已发送");

        if (success) {
            return ResponseResult.success(result, "验证码已发送");
        } else {
            return ResponseResult.fail("500", "验证码发送失败，请稍后重试");
        }
    }

    @ApiOperation("验证验证码")
    @PostMapping("/verify")
    public ResponseResult<Map<String, Object>> verifyCode(
            @ApiParam("手机号") @RequestParam String phone,
            @ApiParam("验证码") @RequestParam String code,
            @ApiParam("验证码类型: register-注册, login-登录, reset-重置密码, payment-支付密码")
            @RequestParam(defaultValue = VerificationType.REGISTER) String type) {

        boolean valid = verificationService.verifyCode(phone, code, type);

        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        result.put("phone", phone);
        result.put("type", type);

        if (valid) {
            return ResponseResult.success(result, "验证成功");
        } else {
            return ResponseResult.fail("400", "验证码错误或已过期");
        }
    }
}
