package com.xx.jaseatschoicejava.constants;

/**
 * 验证码类型常量
 */
public class VerificationType {

    /**
     * 注册验证码
     */
    public static final String REGISTER = "register";

    /**
     * 登录验证码
     */
    public static final String LOGIN = "login";

    /**
     * 重置密码验证码
     */
    public static final String RESET_PASSWORD = "reset";

    /**
     * 支付密码验证码
     */
    public static final String PAYMENT = "payment";

    /**
     * 修改手机号验证码
     */
    public static final String CHANGE_PHONE = "change_phone";

    /**
     * 默认验证码类型
     */
    public static final String DEFAULT = REGISTER;

    private VerificationType() {
        // 防止实例化
    }
}
