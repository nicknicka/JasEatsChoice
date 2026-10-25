package com.xx.jaseatschoicejava.service;

/**
 * 支付密码服务接口
 */
public interface PaymentPasswordService {

    /**
     * 设置支付密码
     * @param userId 用户ID
     * @param password 支付密码（加密前）
     * @throws RuntimeException 如果用户已设置支付密码
     */
    void setupPaymentPassword(String userId, String password);

    /**
     * 修改支付密码
     * @param userId 用户ID
     * @param oldPassword 旧支付密码（加密前）
     * @param newPassword 新支付密码（加密前）
     * @throws RuntimeException 如果旧密码不正确
     */
    void changePaymentPassword(String userId, String oldPassword, String newPassword);

    /**
     * 验证支付密码
     * @param userId 用户ID
     * @param password 支付密码（加密前）
     * @return 是否正确
     */
    boolean verifyPaymentPassword(String userId, String password);

    /**
     * 重置支付密码（通过手机验证码）
     * @param userId 用户ID
     * @param newPassword 新支付密码（加密前）
     * @param verificationCode 手机验证码
     * @throws RuntimeException 如果验证码不正确
     */
    void resetPaymentPassword(String userId, String newPassword, String verificationCode);

    /**
     * 检查是否已设置支付密码
     * @param userId 用户ID
     * @return 是否已设置
     */
    boolean hasPaymentPassword(String userId);
}
