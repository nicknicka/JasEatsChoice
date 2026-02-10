package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 钱包安全Controller
 */
@Api(tags = "钱包安全管理")
@RestController
@RequestMapping("/v1/wallet")
@RequiredArgsConstructor
public class WalletSecurityController {

    private final WalletService walletService;

    @ApiOperation("更新钱包锁定状态")
    @PutMapping("/lock-status")
    public ResponseResult<Map<String, Object>> updateWalletLockStatus(
            @ApiParam("用户ID") @RequestParam String userId,
            @ApiParam("是否锁定") @RequestParam Boolean locked) {

        boolean success = walletService.updateWalletLockStatus(userId, locked);

        Map<String, Object> result = Map.of(
                "userId", userId,
                "locked", locked
        );

        if (success) {
            return ResponseResult.success(result, locked ? "钱包已锁定" : "钱包已解锁");
        } else {
            return ResponseResult.fail("500", "操作失败");
        }
    }

    @ApiOperation("获取钱包安全设置")
    @GetMapping("/security-settings")
    public ResponseResult<Map<String, Object>> getWalletSecuritySettings(
            @ApiParam("用户ID") @RequestParam String userId) {

        Map<String, Object> settings = walletService.getWalletSecuritySettings(userId);
        return ResponseResult.success(settings, "获取成功");
    }
}
