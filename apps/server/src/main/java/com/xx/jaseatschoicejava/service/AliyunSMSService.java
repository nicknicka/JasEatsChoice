package com.xx.jaseatschoicejava.service;

/**
 * 阿里云短信认证服务接口
 */
public interface AliyunSMSService {

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @throws Exception 发送失败时抛出异常
     */
    void sendSmsVerifyCode(String phone, String code) throws Exception;
}