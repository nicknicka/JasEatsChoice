package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Wallet;
import com.xx.jaseatschoicejava.entity.WithdrawRecord;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.service.WithdrawRecordService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/**
 * 钱包控制器
 * <p>
 * 身份一律取自已验签的 JWT：路径/参数里的 userId 只为兼容既有前端保留，不参与鉴权。
 * 充值只允许管理端手动入账（真实支付回调尚未接入），用户侧不能再自行加余额。
 */
@Slf4j
@Api(tags = "钱包管理")
@RestController
@RequestMapping("/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WithdrawRecordService withdrawRecordService;

    /**
     * 获取用户钱包信息
     */
    @ApiOperation("获取用户钱包信息")
    @GetMapping("/info/{userId}")
    public ResponseResult<?> getWalletInfo(@PathVariable String userId, HttpServletRequest request) {
        String targetUserId = resolveSelfOrAdmin(userId, request);
        try {
            Wallet wallet = walletService.getWalletByUserId(targetUserId);
            return ResponseResult.success(wallet);
        } catch (Exception e) {
            log.error("获取钱包信息失败，用户ID：{}", targetUserId, e);
            return ResponseResult.fail("500", "获取钱包信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户余额
     */
    @ApiOperation("获取用户余额")
    @GetMapping("/balance/{userId}")
    public ResponseResult<?> getBalance(@PathVariable String userId, HttpServletRequest request) {
        String targetUserId = resolveSelfOrAdmin(userId, request);
        try {
            BigDecimal balance = walletService.getBalance(targetUserId);
            return ResponseResult.success(balance);
        } catch (Exception e) {
            log.error("获取余额失败，用户ID：{}", targetUserId, e);
            return ResponseResult.fail("500", "获取余额失败：" + e.getMessage());
        }
    }

    /**
     * 检查余额是否足够
     */
    @ApiOperation("检查余额是否足够")
    @GetMapping("/check")
    public ResponseResult<?> checkBalance(
        @ApiParam("用户ID") @RequestParam(required = false) String userId,
        @ApiParam("金额") @RequestParam BigDecimal amount,
        HttpServletRequest request
    ) {
        String targetUserId = resolveSelfOrAdmin(userId, request);
        try {
            boolean enough = walletService.checkBalance(targetUserId, amount);
            return ResponseResult.success(enough);
        } catch (Exception e) {
            log.error("检查余额失败，用户ID：{}，金额：{}", targetUserId, amount, e);
            return ResponseResult.fail("500", "检查余额失败：" + e.getMessage());
        }
    }

    /**
     * 充值
     * <p>
     * 只允许管理端手动入账：用户侧没有任何支付渠道能证明资金到账，
     * 放开给用户等于凭空造钱。真实支付回调接入后再增加对应的验签入口。
     */
    @ApiOperation("钱包充值（管理端）")
    @PostMapping("/recharge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseResult<?> recharge(
        @ApiParam("用户ID") @RequestParam String userId,
        @ApiParam("充值金额") @RequestParam BigDecimal amount,
        @ApiParam("充值流水号") @RequestParam String rechargeNo,
        HttpServletRequest request
    ) {
        log.info("管理端充值，操作者：{}，目标用户：{}，金额：{}，流水号：{}",
                UserIdentityUtil.extractVerifiedUserId(request), userId, amount, rechargeNo);
        try {
            Wallet wallet = walletService.recharge(userId, amount, rechargeNo);
            return ResponseResult.success(wallet);
        } catch (IllegalArgumentException e) {
            log.warn("充值参数不合法，用户ID：{}，金额：{}，原因：{}", userId, amount, e.getMessage());
            return ResponseResult.fail("400", e.getMessage());
        } catch (Exception e) {
            log.error("充值失败，用户ID：{}，金额：{}", userId, amount, e);
            return ResponseResult.fail("500", "充值失败：" + e.getMessage());
        }
    }

    /**
     * 提现申请（创建提现记录，等待审核）
     * <p>
     * 只允许本人操作：提现是把余额降下去，不接受管理员代他人发起。
     */
    @ApiOperation("钱包提现申请")
    @PostMapping("/withdraw")
    public ResponseResult<?> withdraw(
        @ApiParam("用户ID") @RequestParam(required = false) String userId,
        @ApiParam("提现金额") @RequestParam BigDecimal amount,
        @ApiParam("提现流水号") @RequestParam String withdrawNo,
        @ApiParam("提现方式") @RequestParam(defaultValue = "wechat") String withdrawMethod,
        @ApiParam("提现账号信息") @RequestParam(required = false) String accountInfo,
        HttpServletRequest request
    ) {
        String authenticatedUserId = UserIdentityUtil.requireVerifiedUserId(request);
        if (userId != null && !userId.isBlank() && !userId.equals(authenticatedUserId)) {
            log.warn("拒绝代他人提现，操作者：{}，目标用户：{}", authenticatedUserId, userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能操作本人钱包");
        }
        try {
            // 检查余额是否足够
            if (!walletService.checkBalance(authenticatedUserId, amount)) {
                return ResponseResult.fail("400", "余额不足");
            }

            // 创建提现记录（待审核状态）
            WithdrawRecord record = withdrawRecordService.createWithdrawRequest(
                authenticatedUserId,
                amount,
                withdrawNo,
                withdrawMethod,
                accountInfo != null ? accountInfo : "微信钱包"
            );

            // 通知用户提现申请已提交
            NotificationUtil.createWithdrawNotification(
                authenticatedUserId,
                NotificationTypeEnum.WITHDRAW_REQUEST,
                amount.toString(),
                null
            );

            return ResponseResult.success(record, "提现申请已提交，等待审核");
        } catch (IllegalArgumentException e) {
            log.warn("提现申请被拒绝，用户ID：{}，金额：{}，原因：{}", authenticatedUserId, amount, e.getMessage());
            return ResponseResult.fail("400", e.getMessage());
        } catch (Exception e) {
            log.error("提现申请失败，用户ID：{}，金额：{}", authenticatedUserId, amount, e);
            return ResponseResult.fail("500", "提现申请失败：" + e.getMessage());
        }
    }

    /**
     * 解析读取类接口的目标用户：本人可看自己的，管理员可看任意用户，其它 403。
     */
    private String resolveSelfOrAdmin(String claimedUserId, HttpServletRequest request) {
        String authenticatedUserId = UserIdentityUtil.requireVerifiedUserId(request);
        if (claimedUserId == null || claimedUserId.isBlank() || claimedUserId.equals(authenticatedUserId)) {
            return authenticatedUserId;
        }
        if (!hasAdminRole()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能查看本人钱包");
        }
        return claimedUserId;
    }

    private boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
