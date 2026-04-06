package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.OauthDto;
import com.xx.jaseatschoicejava.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * OAuth 第三方登录 API
 *
 * 端点：
 * POST /v1/oauth/authorize-url     → 获取授权URL
 * POST /v1/oauth/callback          → 授权回调处理
 * POST /v1/oauth/bind-phone        → 新用户绑定手机号
 * POST /v1/oauth/bind              → 已登录用户绑定第三方账号
 * DELETE /v1/oauth/unbind/{provider} → 解绑
 * GET  /v1/oauth/accounts          → 查询已绑定账号列表
 */
@Slf4j
@RestController
@RequestMapping("/v1/oauth")
public class OAuthController {

    @Autowired
    private OAuthService oauthService;

    /**
     * 获取授权URL
     * 前端调用此接口获取授权URL，在 BrowserWindow 中打开
     */
    @PostMapping("/authorize-url")
    public ResponseResult<OauthDto.AuthorizeUrlResponse> getAuthorizeUrl(
            @RequestBody OauthDto.AuthorizeUrlRequest request) {
        try {
            String provider = request.getProvider();
            if (!"wechat".equals(provider) && !"qq".equals(provider)) {
                return ResponseResult.fail("400", "不支持的平台：" + provider);
            }
            OauthDto.AuthorizeUrlResponse response = oauthService.getAuthorizeUrl(provider);
            return ResponseResult.success(response);
        } catch (Exception e) {
            log.error("[OAuthController] 获取授权URL失败", e);
            return ResponseResult.fail("500", "获取授权URL失败");
        }
    }

    /**
     * 授权回调处理
     * 前端在 BrowserWindow 拦截到 code 后调用此接口
     */
    @PostMapping("/callback")
    public ResponseResult<OauthDto.LoginResult> handleCallback(
            @RequestBody OauthDto.CallbackRequest request) {
        try {
            OauthDto.LoginResult result = oauthService.handleCallback(request);
            if (result.getSuccess()) {
                return ResponseResult.success(result);
            } else {
                return ResponseResult.fail("400", "授权登录失败");
            }
        } catch (Exception e) {
            log.error("[OAuthController] 授权回调处理失败", e);
            return ResponseResult.fail("500", "授权登录异常");
        }
    }

    /**
     * 新用户绑定手机号完成注册
     * 首次第三方登录后，引导绑定手机号
     */
    @PostMapping("/bind-phone")
    public ResponseResult<OauthDto.LoginResult> bindPhone(
            @RequestBody OauthDto.BindPhoneRequest request) {
        try {
            if (request.getTempToken() == null || request.getPhone() == null
                    || request.getSmsCode() == null) {
                return ResponseResult.fail("400", "参数不完整");
            }
            OauthDto.LoginResult result = oauthService.bindPhone(request);
            if (result.getSuccess()) {
                return ResponseResult.success(result, "注册成功");
            } else {
                return ResponseResult.fail("400", "绑定手机号失败");
            }
        } catch (Exception e) {
            log.error("[OAuthController] 绑定手机号失败", e);
            return ResponseResult.fail("500", "绑定手机号异常");
        }
    }

    /**
     * 已登录用户绑定第三方账号
     * 需要在 Header 中携带 Bearer token
     */
    @PostMapping("/bind")
    public ResponseResult<?> bindOAuthAccount(
            HttpServletRequest httpRequest,
            @RequestBody OauthDto.BindRequest request) {
        try {
            String userId = extractUserId(httpRequest);
            if (userId == null) {
                return ResponseResult.fail("401", "未登录");
            }
            boolean success = oauthService.bindOAuthAccount(userId, request);
            if (success) {
                return ResponseResult.success(null, "绑定成功");
            } else {
                return ResponseResult.fail("400", "绑定失败，该账号可能已被其他用户绑定");
            }
        } catch (Exception e) {
            log.error("[OAuthController] 绑定第三方账号失败", e);
            return ResponseResult.fail("500", "绑定异常");
        }
    }

    /**
     * 解绑第三方账号
     */
    @DeleteMapping("/unbind/{provider}")
    public ResponseResult<?> unbindOAuthAccount(
            HttpServletRequest httpRequest,
            @PathVariable String provider) {
        try {
            String userId = extractUserId(httpRequest);
            if (userId == null) {
                return ResponseResult.fail("401", "未登录");
            }
            boolean success = oauthService.unbindOAuthAccount(userId, provider);
            if (success) {
                return ResponseResult.success(null, "解绑成功");
            } else {
                return ResponseResult.fail("400", "解绑失败");
            }
        } catch (Exception e) {
            log.error("[OAuthController] 解绑第三方账号失败", e);
            return ResponseResult.fail("500", "解绑异常");
        }
    }

    /**
     * 查询已绑定的第三方账号列表
     */
    @GetMapping("/accounts")
    public ResponseResult<List<OauthDto.BoundAccountInfo>> getBoundAccounts(
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            if (userId == null) {
                return ResponseResult.fail("401", "未登录");
            }
            List<OauthDto.BoundAccountInfo> accounts = oauthService.getBoundAccounts(userId);
            return ResponseResult.success(accounts);
        } catch (Exception e) {
            log.error("[OAuthController] 查询绑定账号失败", e);
            return ResponseResult.fail("500", "查询异常");
        }
    }

    /**
     * 从请求中提取用户ID
     */
    private String extractUserId(HttpServletRequest request) {
        // 从 SecurityContext 获取（由 JwtAuthenticationFilter 设置）
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }
}
