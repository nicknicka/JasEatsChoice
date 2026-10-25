package com.xx.jaseatschoicejava.entity;

import lombok.Data;

/**
 * 商家注册请求参数
 */
@Data
public class MerchantRegisterRequest {
    /**
     * 商家名称
     */
    private String name;

    /**
     * 营业执照号
     */
    private String businessLicense;

    /**
     * 经营范围
     */
    private String[] businessScope;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码会话key
     */
    private String captchaKey;
}
