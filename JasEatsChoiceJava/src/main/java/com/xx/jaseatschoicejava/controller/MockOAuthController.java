package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.MockOAuthUser;
import com.xx.jaseatschoicejava.mapper.MockOAuthUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 模拟 OAuth 授权服务器（毕设演示用）
 *
 * 模拟微信/QQ的OAuth2.0授权流程：
 * 1. 显示模拟授权页面（GET /mock/oauth/{provider}/authorize）
 * 2. 用户输入模拟账号密码确认授权（POST /mock/oauth/{provider}/authorize）
 * 3. 用 code 换 token（GET /mock/oauth/{provider}/token）
 * 4. 用 token 获取用户信息（GET /mock/oauth/{provider}/userinfo）
 */
@Slf4j
@RestController
@RequestMapping("/mock/oauth")
public class MockOAuthController {

    @Autowired
    private MockOAuthUserMapper mockOAuthUserMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String MOCK_CODE_PREFIX = "mock:code:";
    private static final String MOCK_TOKEN_PREFIX = "mock:token:";

    /**
     * 模拟授权页面（GET请求返回HTML）
     */
    @GetMapping(value = "/{provider}/authorize", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    @ResponseBody
    public String authorizePage(
            @PathVariable String provider,
            @RequestParam String state,
            @RequestParam String redirect_uri) {

        String platformName = "wechat".equals(provider) ? "微信" : "QQ";
        String themeColor = "wechat".equals(provider) ? "#07C160" : "#12B7F5";
        String logo = "wechat".equals(provider)
                ? "<svg width=\"48\" height=\"48\" viewBox=\"0 0 24 24\" fill=\"none\"><path d=\"M9.5 4C5.36 4 2 6.69 2 10c0 1.89 1.08 3.56 2.78 4.66L4 17l2.5-1.18C7.45 16.07 8.46 16.2 9.5 16.2c.34 0 .67-.02 1-.06C10.17 15.7 10 15.12 10 14.5 10 11.47 12.69 9 16 9c.35 0 .69.03 1.02.08C16.43 6.15 13.27 4 9.5 4zM7 9a1 1 0 110-2 1 1 0 010 2zm5 0a1 1 0 110-2 1 1 0 010 2z\" fill=\"#07C160\"/><path d=\"M22 14.5c0-2.49-2.46-4.5-5.5-4.5S11 12.01 11 14.5 13.46 19 16.5 19c.86 0 1.67-.15 2.39-.42L21 20l-.58-2.11C21.37 16.95 22 15.79 22 14.5zm-7-1a.75.75 0 110-1.5.75.75 0 010 1.5zm3.5 0a.75.75 0 110-1.5.75.75 0 010 1.5z\" fill=\"#07C160\"/></svg>"
                : "<svg width=\"48\" height=\"48\" viewBox=\"0 0 24 24\" fill=\"none\"><path d=\"M12 2C8.13 2 5 5.13 5 9.5c0 2.38 1 4.28 2.5 5.5v3.5l2.83-1.42C11.18 17.36 11.57 17.5 12 17.5c.43 0 .82-.14 1.67-.42L16.5 18.5V15c1.5-1.22 2.5-3.12 2.5-5.5C19 5.13 15.87 2 12 2z\" fill=\"#12B7F5\"/><circle cx=\"9.8\" cy=\"9\" r=\"1.3\" fill=\"white\"/><circle cx=\"14.2\" cy=\"9\" r=\"1.3\" fill=\"white\"/><path d=\"M7 15.5c-1.5 1.5-1 3.5.5 4.5\" stroke=\"#12B7F5\" stroke-width=\"1.2\" stroke-linecap=\"round\"/><path d=\"M17 15.5c1.5 1.5 1 3.5-.5 4.5\" stroke=\"#12B7F5\" stroke-width=\"1.2\" stroke-linecap=\"round\"/></svg>";

        return "<!DOCTYPE html>\n" +
                "<html><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>" + platformName + " 授权登录</title>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif; background: #f5f5f5; min-height: 100vh; display: flex; justify-content: center; align-items: center; }" +
                ".card { background: white; border-radius: 16px; padding: 40px 32px; width: 320px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); text-align: center; }" +
                ".logo { margin-bottom: 16px; }" +
                "h2 { font-size: 18px; color: #333; margin-bottom: 8px; }" +
                "p { font-size: 13px; color: #999; margin-bottom: 24px; }" +
                "input { width: 100%; padding: 12px 16px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; margin-bottom: 12px; outline: none; transition: border-color 0.2s; }" +
                "input:focus { border-color: " + themeColor + "; }" +
                "button { width: 100%; padding: 12px; background: " + themeColor + "; color: white; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; transition: opacity 0.2s; }" +
                "button:hover { opacity: 0.9; }" +
                ".hint { font-size: 11px; color: #bbb; margin-top: 16px; }" +
                ".error { color: #e74c3c; font-size: 12px; margin-bottom: 8px; display: none; }" +
                "</style></head><body>" +
                "<div class='card'>" +
                "<div class='logo'>" + logo + "</div>" +
                "<h2>" + platformName + " 授权登录</h2>" +
                "<p>佳食宜选 请求访问你的" + platformName + "账号信息</p>" +
                "<div class='error' id='error'></div>" +
                "<form id='authForm' method='POST' action='/api/mock/oauth/" + provider + "/authorize'>" +
                "<input type='hidden' name='state' value='" + state + "'>" +
                "<input type='hidden' name='redirect_uri' value='" + redirect_uri + "'>" +
                "<input type='text' name='username' id='username' placeholder='模拟账号（昵称，如：微信用户小明）' required>" +
                "<input type='password' name='password' id='password' placeholder='密码（预置账号均为 123456）' value='123456'>" +
                "<button type='submit'>确认授权</button>" +
                "</form>" +
                "<div class='hint'>模拟授权服务器（毕设演示）</div>" +
                "</div></body></html>";
    }

    /**
     * 处理模拟授权表单提交，生成 code 并重定向
     */
    @PostMapping(value = "/{provider}/authorize", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    @ResponseBody
    public String authorizeSubmit(
            @PathVariable String provider,
            @RequestParam String state,
            @RequestParam String redirect_uri,
            @RequestParam String username,
            @RequestParam(required = false) String password) {

        // 查找模拟用户（按昵称匹配）
        MockOAuthUser mockUser = mockOAuthUserMapper.selectOne(
                new LambdaQueryWrapper<MockOAuthUser>()
                        .eq(MockOAuthUser::getProvider, provider)
                        .eq(MockOAuthUser::getNickname, username)
        );

        if (mockUser == null) {
            // 尝试按 openId 匹配
            mockUser = mockOAuthUserMapper.selectOne(
                    new LambdaQueryWrapper<MockOAuthUser>()
                            .eq(MockOAuthUser::getProvider, provider)
                            .eq(MockOAuthUser::getOpenId, username)
            );
        }

        if (mockUser == null) {
            String platformName = "wechat".equals(provider) ? "微信" : "QQ";
            return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                    "<style>body{font-family:sans-serif;display:flex;justify-content:center;align-items:center;min-height:100vh;background:#f5f5f5;}" +
                    ".card{background:white;padding:32px;border-radius:12px;text-align:center;box-shadow:0 2px 12px rgba(0,0,0,0.08);}" +
                    "a{color:#07C160;text-decoration:none;}</style></head>" +
                    "<body><div class='card'>" +
                    "<h3>用户不存在</h3><p>未找到模拟用户：" + username + "</p>" +
                    "<p style='font-size:12px;color:#999;margin-top:8px;'>请使用预置的模拟账号登录</p>" +
                    "<a href='javascript:history.back()'>返回重试</a>" +
                    "</div></body></html>";
        }

        // 验证密码（预置账号密码均为123456，允许空密码）
        if (mockUser.getPassword() != null && !mockUser.getPassword().isEmpty()
                && password != null && !password.equals(mockUser.getPassword())) {
            return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                    "<style>body{font-family:sans-serif;display:flex;justify-content:center;align-items:center;min-height:100vh;background:#f5f5f5;}" +
                    ".card{background:white;padding:32px;border-radius:12px;text-align:center;box-shadow:0 2px 12px rgba(0,0,0,0.08);}" +
                    "a{color:#07C160;text-decoration:none;}</style></head>" +
                    "<body><div class='card'>" +
                    "<h3>密码错误</h3><p>模拟账号密码不正确</p>" +
                    "<a href='javascript:history.back()'>返回重试</a>" +
                    "</div></body></html>";
        }

        // 生成模拟 authorization code
        String code = "mock_code_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 将 code 与模拟用户信息存入 Redis（5分钟有效）
        String codeKey = MOCK_CODE_PREFIX + code;
        String codeValue = mockUser.getProvider() + ":" + mockUser.getOpenId();
        redisTemplate.opsForValue().set(codeKey, codeValue, 5, TimeUnit.MINUTES);

        log.info("[MockOAuth] 模拟授权成功, provider={}, nickname={}, code={}", provider, mockUser.getNickname(), code);

        // 重定向到 redirect_uri 并携带 code 和 state
        String separator = redirect_uri.contains("?") ? "&" : "?";
        String redirectUrl = redirect_uri + separator + "code=" + code + "&state=" + state;

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<meta http-equiv='refresh' content='0;url=" + redirectUrl + "'>" +
                "<style>body{display:flex;justify-content:center;align-items:center;min-height:100vh;font-family:sans-serif;color:#666;}</style>" +
                "</head><body><p>授权成功，正在跳转...</p></body></html>";
    }

    /**
     * 模拟用 code 换 access_token
     * 返回格式与真实平台一致
     */
    @GetMapping("/{provider}/token")
    @ResponseBody
    public Map<String, Object> token(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String appid,
            @RequestParam String secret,
            @RequestParam String grant_type) {

        String codeKey = MOCK_CODE_PREFIX + code;
        String codeValue = redisTemplate.opsForValue().get(codeKey);

        if (codeValue == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("errcode", 40029);
            error.put("errmsg", "invalid code");
            return error;
        }

        // 解析存储的用户信息
        String[] parts = codeValue.split(":");
        String storedProvider = parts[0];
        String openId = parts[1];

        // code 只能用一次，立即删除
        redisTemplate.delete(codeKey);

        // 查找模拟用户完整信息
        MockOAuthUser mockUser = mockOAuthUserMapper.selectOne(
                new LambdaQueryWrapper<MockOAuthUser>()
                        .eq(MockOAuthUser::getProvider, storedProvider)
                        .eq(MockOAuthUser::getOpenId, openId)
        );

        if (mockUser == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("errcode", 40003);
            error.put("errmsg", "user not found");
            return error;
        }

        // 生成模拟 access_token
        String accessToken = "mock_token_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String refreshToken = "mock_refresh_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);

        // 存储 token 与用户映射（2小时有效）
        String tokenKey = MOCK_TOKEN_PREFIX + accessToken;
        redisTemplate.opsForValue().set(tokenKey, codeValue, 2, TimeUnit.HOURS);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("access_token", accessToken);
        result.put("expires_in", 7200);
        result.put("refresh_token", refreshToken);
        result.put("openid", openId);
        if (mockUser.getUnionId() != null) {
            result.put("unionid", mockUser.getUnionId());
        }
        result.put("scope", "snsapi_login");

        log.info("[MockOAuth] 模拟token换取成功, provider={}, openId={}", provider, openId);
        return result;
    }

    /**
     * 模拟用 access_token 获取用户信息
     */
    @GetMapping("/{provider}/userinfo")
    @ResponseBody
    public Map<String, Object> userinfo(
            @PathVariable String provider,
            @RequestParam String access_token,
            @RequestParam String openid) {

        String tokenKey = MOCK_TOKEN_PREFIX + access_token;
        String tokenValue = redisTemplate.opsForValue().get(tokenKey);

        if (tokenValue == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("errcode", 42001);
            error.put("errmsg", "access_token expired");
            return error;
        }

        // 查找模拟用户
        MockOAuthUser mockUser = mockOAuthUserMapper.selectOne(
                new LambdaQueryWrapper<MockOAuthUser>()
                        .eq(MockOAuthUser::getProvider, provider)
                        .eq(MockOAuthUser::getOpenId, openid)
        );

        if (mockUser == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("errcode", 40003);
            error.put("errmsg", "user not found");
            return error;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("openid", mockUser.getOpenId());
        result.put("nickname", mockUser.getNickname());
        result.put("headimgurl", mockUser.getAvatarUrl());
        result.put("sex", 0);
        result.put("province", "广东");
        result.put("city", "深圳");
        result.put("country", "中国");
        if (mockUser.getUnionId() != null) {
            result.put("unionid", mockUser.getUnionId());
        }
        result.put("privilege", new ArrayList<>());

        log.info("[MockOAuth] 模拟获取用户信息成功, provider={}, nickname={}", provider, mockUser.getNickname());
        return result;
    }
}
