package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.entity.Wallet;

import java.math.BigDecimal;

/**
 * 钱包服务接口
 */
public interface WalletService {

    /**
     * 根据用户ID获取钱包
     * @param userId 用户ID
     * @return 钱包信息
     */
    Wallet getWalletByUserId(Long userId);

    /**
     * 获取用户余额
     * @param userId 用户ID
     * @return 当前余额
     */
    BigDecimal getBalance(Long userId);

    /**
     * 充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @param rechargeNo 充值流水号
     * @return 充值后的钱包
     */
    Wallet recharge(Long userId, BigDecimal amount, String rechargeNo);

    /**
     * 扣减余额（用于支付）
     * @param userId 用户ID
     * @param amount 扣减金额
     * @param description 描述
     * @return 是否成功
     */
    boolean deductBalance(Long userId, BigDecimal amount, String description);

    /**
     * 提现
     * @param userId 用户ID
     * @param amount 提现金额
     * @param withdrawNo 提现流水号
     * @return 是否成功
     */
    boolean withdraw(Long userId, BigDecimal amount, String withdrawNo);

    /**
     * 退款（增加余额）
     * @param userId 用户ID
     * @param amount 退款金额
     * @param description 描述
     * @return 是否成功
     */
    boolean refund(Long userId, BigDecimal amount, String description);

    /**
     * 创建用户钱包
     * @param userId 用户ID
     * @return 创建的钱包
     */
    Wallet createWallet(Long userId);

    /**
     * 检查余额是否足够
     * @param userId 用户ID
     * @param amount 金额
     * @return 是否足够
     */
    boolean checkBalance(Long userId, BigDecimal amount);

    /**
     * 冻结钱包
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean freezeWallet(Long userId);

    /**
     * 解冻钱包
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unfreezeWallet(Long userId);
}
