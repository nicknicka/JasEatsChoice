package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.ConsumeHistory;
import com.xx.jaseatschoicejava.entity.Wallet;
import com.xx.jaseatschoicejava.mapper.WalletMapper;
import com.xx.jaseatschoicejava.service.ConsumeHistoryService;
import com.xx.jaseatschoicejava.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final ConsumeHistoryService consumeHistoryService;

    @Override
    public Wallet getWalletByUserId(String userId) {
        LambdaQueryWrapper<Wallet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Wallet::getUserId, userId);
        Wallet wallet = walletMapper.selectOne(queryWrapper);

        // 如果钱包不存在，自动创建
        if (wallet == null) {
            wallet = createWallet(userId);
        }

        return wallet;
    }

    @Override
    public BigDecimal getBalance(String userId) {
        Wallet wallet = getWalletByUserId(userId);
        return wallet != null ? wallet.getBalance() : BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Wallet recharge(String userId, BigDecimal amount, String rechargeNo) {
        // 参数校验
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }

        // 获取钱包
        Wallet wallet = getWalletByUserId(userId);

        // 更新余额和累计充值
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalRecharge(wallet.getTotalRecharge().add(amount));
        wallet.setUpdateTime(LocalDateTime.now());

        // 更新数据库（使用乐观锁）
        int rows = walletMapper.updateById(wallet);
        if (rows == 0) {
            throw new RuntimeException("充值失败，请重试");
        }

        // 记录消费历史
        ConsumeHistory history = new ConsumeHistory();
        history.setUserId(userId);
        history.setType("recharge");
        history.setAmount(amount);
        history.setDescription("钱包充值 - " + rechargeNo);
        history.setStatus("success");
        history.setCreateTime(LocalDateTime.now());
        consumeHistoryService.save(history);

        log.info("用户{}充值成功，金额：{}，流水号：{}", userId, amount, rechargeNo);
        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(String userId, BigDecimal amount, String description) {
        // 参数校验
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("扣减金额必须大于0");
        }

        // 获取钱包
        Wallet wallet = getWalletByUserId(userId);

        // 检查钱包状态
        if ("frozen".equals(wallet.getStatus())) {
            throw new RuntimeException("钱包已冻结，无法进行交易");
        }

        // 检查余额
        if (!checkBalance(userId, amount)) {
            throw new RuntimeException("余额不足");
        }

        // 扣减余额
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setTotalConsume(wallet.getTotalConsume().add(amount));
        wallet.setUpdateTime(LocalDateTime.now());

        // 更新数据库（使用乐观锁）
        int rows = walletMapper.updateById(wallet);
        if (rows == 0) {
            throw new RuntimeException("扣费失败，请重试");
        }

        // 记录消费历史
        ConsumeHistory history = new ConsumeHistory();
        history.setUserId(userId);
        history.setType("consume");
        history.setAmount(amount);
        history.setDescription(description);
        history.setStatus("success");
        history.setCreateTime(LocalDateTime.now());
        consumeHistoryService.save(history);

        log.info("用户{}扣费成功，金额：{}，描述：{}", userId, amount, description);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdraw(String userId, BigDecimal amount, String withdrawNo) {
        // 参数校验
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("提现金额必须大于0");
        }

        // 获取钱包
        Wallet wallet = getWalletByUserId(userId);

        // 检查钱包状态
        if ("frozen".equals(wallet.getStatus())) {
            throw new RuntimeException("钱包已冻结，无法进行交易");
        }

        // 检查余额
        if (!checkBalance(userId, amount)) {
            throw new RuntimeException("余额不足");
        }

        // 扣减余额
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setTotalWithdraw(wallet.getTotalWithdraw().add(amount));
        wallet.setUpdateTime(LocalDateTime.now());

        // 更新数据库（使用乐观锁）
        int rows = walletMapper.updateById(wallet);
        if (rows == 0) {
            throw new RuntimeException("提现失败，请重试");
        }

        // 记录消费历史
        ConsumeHistory history = new ConsumeHistory();
        history.setUserId(userId);
        history.setType("withdraw");
        history.setAmount(amount);
        history.setDescription("钱包提现 - " + withdrawNo);
        history.setStatus("success");
        history.setCreateTime(LocalDateTime.now());
        consumeHistoryService.save(history);

        log.info("用户{}提现成功，金额：{}，流水号：{}", userId, amount, withdrawNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refund(String userId, BigDecimal amount, String description) {
        // 参数校验
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }

        // 获取钱包
        Wallet wallet = getWalletByUserId(userId);

        // 增加余额
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdateTime(LocalDateTime.now());

        // 更新数据库（使用乐观锁）
        int rows = walletMapper.updateById(wallet);
        if (rows == 0) {
            throw new RuntimeException("退款失败，请重试");
        }

        // 记录消费历史
        ConsumeHistory history = new ConsumeHistory();
        history.setUserId(userId);
        history.setType("recharge");
        history.setAmount(amount);
        history.setDescription(description);
        history.setStatus("success");
        history.setCreateTime(LocalDateTime.now());
        consumeHistoryService.save(history);

        log.info("用户{}退款成功，金额：{}，描述：{}", userId, amount, description);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Wallet createWallet(String userId) {
        // 直接查询数据库，避免调用getWalletByUserId导致无限递归
        LambdaQueryWrapper<Wallet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Wallet::getUserId, userId);
        Wallet existWallet = walletMapper.selectOne(queryWrapper);

        if (existWallet != null) {
            return existWallet;
        }

        // 创建新钱包
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setTotalRecharge(BigDecimal.ZERO);
        wallet.setTotalConsume(BigDecimal.ZERO);
        wallet.setTotalWithdraw(BigDecimal.ZERO);
        wallet.setVersion(0);
        wallet.setStatus("active");
        wallet.setCreateTime(LocalDateTime.now());
        wallet.setUpdateTime(LocalDateTime.now());

        walletMapper.insert(wallet);
        log.info("为用户{}创建钱包成功", userId);
        return wallet;
    }

    @Override
    public boolean checkBalance(String userId, BigDecimal amount) {
        BigDecimal balance = getBalance(userId);
        return balance.compareTo(amount) >= 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeWallet(String userId) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setStatus("frozen");
        wallet.setUpdateTime(LocalDateTime.now());
        int rows = walletMapper.updateById(wallet);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeWallet(String userId) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setStatus("active");
        wallet.setUpdateTime(LocalDateTime.now());
        int rows = walletMapper.updateById(wallet);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWalletLockStatus(String userId, boolean locked) {
        Wallet wallet = getWalletByUserId(userId);

        // 如果锁定，还需冻结钱包状态
        if (locked) {
            wallet.setStatus("frozen");
        } else if ("frozen".equals(wallet.getStatus()) && !locked) {
            wallet.setStatus("active");
        }

        wallet.setLocked(locked);
        wallet.setUpdateTime(LocalDateTime.now());
        int rows = walletMapper.updateById(wallet);

        log.info("用户{}钱包锁定状态已更新为：{}", userId, locked);
        return rows > 0;
    }

    @Override
    public java.util.Map<String, Object> getWalletSecuritySettings(String userId) {
        Wallet wallet = getWalletByUserId(userId);

        java.util.Map<String, Object> settings = new java.util.HashMap<>();
        settings.put("userId", userId);
        settings.put("locked", wallet.getLocked() != null ? wallet.getLocked() : false);
        settings.put("verifyEnabled", wallet.getVerifyEnabled() != null ? wallet.getVerifyEnabled() : true);
        settings.put("dailyLimit", wallet.getDailyLimit() != null ? wallet.getDailyLimit() : new BigDecimal("5000"));

        return settings;
    }
}
