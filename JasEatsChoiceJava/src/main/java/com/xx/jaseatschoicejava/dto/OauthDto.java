package com.xx.jaseatschoicejava.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * OAuth 用户信息 DTO
 */
@Data
@NoArgsConstructor
public class OauthDto {

    /** 获取授权URL请求 */
    @Data
    @NoArgsConstructor
    public static class AuthorizeUrlRequest {
        private String provider; // "wechat" | "qq"
    }

    /** 获取授权URL响应 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorizeUrlResponse {
        private String authUrl;
        private String state;
        private String codeVerifier;
    }

    /** 授权回调请求 */
    @Data
    @NoArgsConstructor
    public static class CallbackRequest {
        private String provider;
        private String code;
        private String state;
        private String codeVerifier;
    }

    /** 绑定手机号请求 */
    @Data
    @NoArgsConstructor
    public static class BindPhoneRequest {
        private String tempToken;
        private String phone;
        private String smsCode;
    }

    /** 已登录用户绑定第三方账号请求 */
    @Data
    @NoArgsConstructor
    public static class BindRequest {
        private String provider;
        private String code;
        private String state;
        private String codeVerifier;
    }

    /** OAuth 登录结果 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResult {
        private Boolean success;
        private Boolean needBindPhone;
        private String tempToken;       // needBindPhone=true 时有效
        private String token;           // needBindPhone=false 时有效
        private Object user;            // 已关联用户信息
        private String oauthNickname;   // 第三方昵称
        private String oauthAvatar;     // 第三方头像
    }

    /** 已绑定账号信息 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundAccountInfo {
        private String provider;
        private String nickname;
        private String avatarUrl;
        private String bindTime;
    }
}
