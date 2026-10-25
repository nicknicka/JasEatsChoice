package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Wallet;
import com.xx.jaseatschoicejava.entity.WithdrawRecord;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.service.WithdrawRecordService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 钱包控制器
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
    public ResponseResult<?> getWalletInfo(@PathVariable String userId) {
        try {
            Wallet wallet = walletService.getWalletByUserId(userId);
            return ResponseResult.success(wallet);
        } catch (Exception e) {
            log.error("获取钱包信息失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取钱包信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户余额
     */
    @ApiOperation("获取用户余额")
    @GetMapping("/balance/{userId}")
    public ResponseResult<?> getBalance(@PathVariable String userId) {
        try {
            BigDecimal balance = walletService.getBalance(userId);
            return ResponseResult.success(balance);
        } catch (Exception e) {
            log.error("获取余额失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取余额失败：" + e.getMessage());
        }
    }

    /**
     * 检查余额是否足够
     */
    @ApiOperation("检查余额是否足够")
    @GetMapping("/check")
    public ResponseResult<?> checkBalance(
        @ApiParam("用户ID") @RequestParam String userId,
        @ApiParam("金额") @RequestParam BigDecimal amount
    ) {
        try {
            boolean enough = walletService.checkBalance(userId, amount);
            return ResponseResult.success(enough);
        } catch (Exception e) {
            log.error("检查余额失败，用户ID：{}，金额：{}", userId, amount, e);
            return ResponseResult.fail("500", "检查余额失败：" + e.getMessage());
        }
    }

    /**
     * 充值
     */
    @ApiOperation("钱包充值")
    @PostMapping("/recharge")
    public ResponseResult<?> recharge(
        @ApiParam("用户ID") @RequestParam String userId,
        @ApiParam("充值金额") @RequestParam BigDecimal amount,
        @ApiParam("充值流水号") @RequestParam String rechargeNo
    ) {
        try {
            Wallet wallet = walletService.recharge(userId, amount, rechargeNo);
            return ResponseResult.success(wallet);
        } catch (Exception e) {
            log.error("充值失败，用户ID：{}，金额：{}", userId, amount, e);
            return ResponseResult.fail("500", "充值失败：" + e.getMessage());
        }
    }

    /**
     * 提现申请（创建提现记录，等待审核）
     */
    @ApiOperation("钱包提现申请")
    @PostMapping("/withdraw")
    public ResponseResult<?> withdraw(
        @ApiParam("用户ID") @RequestParam String userId,
        @ApiParam("提现金额") @RequestParam BigDecimal amount,
        @ApiParam("提现流水号") @RequestParam String withdrawNo,
        @ApiParam("提现方式") @RequestParam(defaultValue = "wechat") String withdrawMethod,
        @ApiParam("提现账号信息") @RequestParam(required = false) String accountInfo
    ) {
        try {
            // 检查余额是否足够
            if (!walletService.checkBalance(userId, amount)) {
                return ResponseResult.fail("400", "余额不足");
            }

            // 创建提现记录（待审核状态）
            WithdrawRecord record = withdrawRecordService.createWithdrawRequest(
                userId,
                amount,
                withdrawNo,
                withdrawMethod,
                accountInfo != null ? accountInfo : "微信钱包"
            );

            // 通知用户提现申请已提交
            NotificationUtil.createWithdrawNotification(
                userId,
                NotificationTypeEnum.WITHDRAW_REQUEST,
                amount.toString(),
                null
            );

            return ResponseResult.success(record, "提现申请已提交，等待审核");
        } catch (Exception e) {
            log.error("提现申请失败，用户ID：{}，金额：{}", userId, amount, e);
            return ResponseResult.fail("500", "提现申请失败：" + e.getMessage());
        }
    }
}
