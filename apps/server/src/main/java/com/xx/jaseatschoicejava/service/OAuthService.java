package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.OauthDto;
import java.util.List;

/**
 * OAuth 第三方登录服务接口
 */
public interface OAuthService {

    /**
     * 获取授权URL
     * @param provider 平台标识 (wechat/qq)
     * @return 授权URL、state、codeVerifier
     */
    OauthDto.AuthorizeUrlResponse getAuthorizeUrl(String provider);

    /**
     * 处理授权回调（登录场景）
     * @param request 回调请求
     * @return 登录结果
     */
    OauthDto.LoginResult handleCallback(OauthDto.CallbackRequest request);

    /**
     * 新用户绑定手机号完成注册
     * @param request 绑定手机号请求
     * @return 登录结果（含token和用户信息）
     */
    OauthDto.LoginResult bindPhone(OauthDto.BindPhoneRequest request);

    /**
     * 已登录用户绑定第三方账号
     * @param userId 用户ID
     * @param request 绑定请求
     * @return 是否成功
     */
    boolean bindOAuthAccount(String userId, OauthDto.BindRequest request);

    /**
     * 解绑第三方账号
     * @param userId 用户ID
     * @param provider 平台标识
     * @return 是否成功
     */
    boolean unbindOAuthAccount(String userId, String provider);

    /**
     * 查询用户已绑定的第三方账号
     * @param userId 用户ID
     * @return 绑定账号列表
     */
    List<OauthDto.BoundAccountInfo> getBoundAccounts(String userId);
}
