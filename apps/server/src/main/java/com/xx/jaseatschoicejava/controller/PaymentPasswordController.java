package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.PaymentPasswordService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * 支付密码控制器
 * <p>
 * 操作对象一律取自已认证身份（{@link UserIdentityUtil#extractVerifiedUserId}），
 * 不再接受请求参数里的 userId，避免越权重置他人支付密码。
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
            @RequestParam String password,
            HttpServletRequest request
    ) {
        String userId = requireUserId(request);
        try {
            paymentPasswordService.setupPaymentPassword(userId, password);
            return ResponseResult.success("支付密码设置成功");
        } catch (IllegalArgumentException e) {
            log.warn("设置支付密码参数不合法，用户ID：{}，原因：{}", userId, e.getMessage());
            return ResponseResult.fail("400", e.getMessage());
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
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            HttpServletRequest request
    ) {
        String userId = requireUserId(request);
        try {
            paymentPasswordService.changePaymentPassword(userId, oldPassword, newPassword);
            return ResponseResult.success("支付密码修改成功");
        } catch (IllegalArgumentException e) {
            log.warn("修改支付密码参数不合法，用户ID：{}，原因：{}", userId, e.getMessage());
            return ResponseResult.fail("400", e.getMessage());
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
            @RequestParam String password,
            HttpServletRequest request
    ) {
        String userId = requireUserId(request);
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
            @RequestParam String newPassword,
            @RequestParam String verificationCode,
            HttpServletRequest request
    ) {
        String userId = requireUserId(request);
        try {
            paymentPasswordService.resetPaymentPassword(userId, newPassword, verificationCode);
            return ResponseResult.success("支付密码重置成功");
        } catch (IllegalArgumentException e) {
            // 短信验证码错误/过期、参数缺失都走 400，不能落到 500
            log.warn("重置支付密码校验未通过，用户ID：{}，原因：{}", userId, e.getMessage());
            return ResponseResult.fail("400", e.getMessage());
        } catch (Exception e) {
            log.error("重置支付密码失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "重置支付密码失败：" + e.getMessage());
        }
    }

    /**
     * 检查是否已设置支付密码
     * <p>
     * 路径里的 userId 只为兼容既有前端与端到端用例保留，不参与鉴权；
     * 实际查询对象依旧是当前已认证用户。
     */
    @ApiOperation("检查是否已设置支付密码")
    @GetMapping("/check/{ignoredUserId}")
    public ResponseResult<?> checkPaymentPassword(@PathVariable String ignoredUserId, HttpServletRequest request) {
        String userId = requireUserId(request);
        try {
            boolean hasPassword = paymentPasswordService.hasPaymentPassword(userId);
            return ResponseResult.success(Map.of("hasPaymentPassword", hasPassword));
        } catch (Exception e) {
            log.error("检查支付密码状态失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "检查支付密码状态失败：" + e.getMessage());
        }
    }

    /**
     * 取已认证身份；未登录或令牌无效时直接 401。
     * 只认 JwtAuthenticationFilter 校验通过后写入的用户ID，不采信任何请求头或参数。
     */
    private String requireUserId(HttpServletRequest request) {
        String userId = UserIdentityUtil.extractVerifiedUserId(request);
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
