package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.PaymentPasswordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付密码控制器
 */
@Slf4j
@Api(tags = "支付密码管理")
@RestController
@RequestMapping("/v1/payment-password")
@RequiredArgsConstructor
public class PaymentPasswordController {

    private final PaymentPasswordService paymentPasswordService;

    /**
     * 设置支付密码
     */
    @ApiOperation("设置支付密码")
    @PostMapping("/setup")
    public ResponseResult<?> setupPaymentPassword(
            @RequestParam String userId,
            @RequestParam String password
    ) {
        try {
            paymentPasswordService.setupPaymentPassword(userId, password);
            return ResponseResult.success("支付密码设置成功");
        } catch (Exception e) {
            log.error("设置支付密码失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "设置支付密码失败：" + e.getMessage());
        }
    }

    /**
     * 修改支付密码
     */
    @ApiOperation("修改支付密码")
    @PostMapping("/change")
    public ResponseResult<?> changePaymentPassword(
            @RequestParam String userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        try {
            paymentPasswordService.changePaymentPassword(userId, oldPassword, newPassword);
            return ResponseResult.success("支付密码修改成功");
        } catch (Exception e) {
            log.error("修改支付密码失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "修改支付密码失败：" + e.getMessage());
        }
    }

    /**
     * 验证支付密码
     */
    @ApiOperation("验证支付密码")
    @PostMapping("/verify")
    public ResponseResult<?> verifyPaymentPassword(
            @RequestParam String userId,
            @RequestParam String password
    ) {
        try {
            boolean valid = paymentPasswordService.verifyPaymentPassword(userId, password);
            return ResponseResult.success(Map.of("valid", valid));
        } catch (Exception e) {
            log.error("验证支付密码失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "验证支付密码失败：" + e.getMessage());
        }
    }

    /**
     * 重置支付密码（通过手机验证码）
     */
    @ApiOperation("重置支付密码")
    @PostMapping("/reset")
    public ResponseResult<?> resetPaymentPassword(
            @RequestParam String userId,
            @RequestParam String newPassword,
            @RequestParam String verificationCode
    ) {
        try {
            paymentPasswordService.resetPaymentPassword(userId, newPassword, verificationCode);
            return ResponseResult.success("支付密码重置成功");
        } catch (Exception e) {
            log.error("重置支付密码失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "重置支付密码失败：" + e.getMessage());
        }
    }

    /**
     * 检查是否已设置支付密码
     */
    @ApiOperation("检查是否已设置支付密码")
    @GetMapping("/check/{userId}")
    public ResponseResult<?> checkPaymentPassword(@PathVariable String userId) {
        try {
            boolean hasPassword = paymentPasswordService.hasPaymentPassword(userId);
            return ResponseResult.success(Map.of("hasPaymentPassword", hasPassword));
        } catch (Exception e) {
            log.error("检查支付密码状态失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "检查支付密码状态失败：" + e.getMessage());
        }
    }
}
