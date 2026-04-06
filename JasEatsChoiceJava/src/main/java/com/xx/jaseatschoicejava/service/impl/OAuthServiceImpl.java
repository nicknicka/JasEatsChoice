package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.config.OAuthConfig;
import com.xx.jaseatschoicejava.dto.OauthDto;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.UserOAuth;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.mapper.UserOAuthMapper;
import com.xx.jaseatschoicejava.service.OAuthService;
import com.xx.jaseatschoicejava.util.IdGenerator;
import com.xx.jaseatschoicejava.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OAuth 第三方登录服务实现
 *
 * 核心流程：
 * 1. 前端请求授权URL → 后端构造URL（含state和PKCE）→ 前端在BrowserWindow中打开
 * 2. 模拟授权服务器返回code → 前端将code发给后端 → 后端用code换取用户信息
 * 3. 查找已关联用户 → 直接登录；未关联 → 返回tempToken引导绑定手机号
 */
@Slf4j
@Service
public class OAuthServiceImpl implements OAuthService {

    @Autowired
    private OAuthConfig oauthConfig;

    @Autowired
    private UserOAuthMapper userOAuthMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private static final String STATE_PREFIX = "oauth:state:";
    private static final String TEMP_PREFIX = "oauth:temp:";
    private static final long STATE_TTL_MINUTES = 5;
    private static final long TEMP_TTL_MINUTES = 30;

    @Override
    public OauthDto.AuthorizeUrlResponse getAuthorizeUrl(String provider) {
        // 生成 state 参数（防CSRF）
        String state = UUID.randomUUID().toString().replace("-", "");

        // 生成 PKCE code_verifier
        String codeVerifier = UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 将 state 和 codeVerifier 存入 Redis
        String stateKey = STATE_PREFIX + state;
        redisTemplate.opsForValue().set(stateKey, codeVerifier, STATE_TTL_MINUTES, TimeUnit.MINUTES);

        // 构造授权URL
        String authUrl = buildAuthUrl(provider, state);

        OauthDto.AuthorizeUrlResponse response = new OauthDto.AuthorizeUrlResponse();
        response.setAuthUrl(authUrl);
        response.setState(state);
        response.setCodeVerifier(codeVerifier);
        return response;
    }

    @Override
    public OauthDto.LoginResult handleCallback(OauthDto.CallbackRequest request) {
        // 1. 验证 state
        String stateKey = STATE_PREFIX + request.getState();
        String storedVerifier = redisTemplate.opsForValue().get(stateKey);
        if (storedVerifier == null) {
            log.warn("[OAuth] state无效或已过期: {}", request.getState());
            return buildErrorResult("授权已过期，请重新登录");
        }
        // state 只能用一次
        redisTemplate.delete(stateKey);

        // 2. 用 code 换取 access_token
        Map<String, Object> tokenResult = exchangeCodeForToken(request.getProvider(), request.getCode());
        if (tokenResult == null || tokenResult.containsKey("errcode")) {
            log.error("[OAuth] code换取token失败: {}", tokenResult);
            return buildErrorResult("授权登录失败，请重试");
        }

        String openId = (String) tokenResult.get("openid");
        String unionId = (String) tokenResult.get("unionid");
        String accessToken = (String) tokenResult.get("access_token");

        // 3. 获取第三方用户信息
        Map<String, Object> userInfo = fetchUserInfo(request.getProvider(), accessToken, openId);
        String nickname = userInfo != null ? (String) userInfo.get("nickname") : "用户";
        String avatarUrl = userInfo != null ? (String) userInfo.get("headimgurl") : "";
        if (unionId == null && userInfo != null) {
            unionId = (String) userInfo.get("unionid");
        }

        // 4. 查找已关联用户
        // 4.1 先通过 openId 查找
        UserOAuth existingOAuth = userOAuthMapper.selectOne(
                new LambdaQueryWrapper<UserOAuth>()
                        .eq(UserOAuth::getProvider, request.getProvider())
                        .eq(UserOAuth::getOpenId, openId)
        );

        if (existingOAuth != null) {
            // 已关联 → 直接登录
            User user = userMapper.selectById(existingOAuth.getUserId());
            if (user != null) {
                // 更新第三方token信息
                updateOAuthToken(existingOAuth, accessToken, tokenResult);

                String token = jwtUtil.generateToken(user.getUserId(), user.getPhone());
                OauthDto.LoginResult result = new OauthDto.LoginResult();
                result.setSuccess(true);
                result.setNeedBindPhone(false);
                result.setToken(token);
                result.setUser(buildUserMap(user));
                return result;
            }
        }

        // 4.2 通过 unionId 查找（打通小程序场景）
        if (unionId != null) {
            UserOAuth unionOAuth = userOAuthMapper.selectOne(
                    new LambdaQueryWrapper<UserOAuth>()
                            .eq(UserOAuth::getProvider, request.getProvider())
                            .eq(UserOAuth::getUnionId, unionId)
            );

            if (unionOAuth != null) {
                User user = userMapper.selectById(unionOAuth.getUserId());
                if (user != null) {
                    // 自动创建桌面端的 OAuth 关联
                    createOAuthRecord(user.getUserId(), request.getProvider(), openId, unionId,
                            nickname, avatarUrl, accessToken, tokenResult);

                    String token = jwtUtil.generateToken(user.getUserId(), user.getPhone());
                    OauthDto.LoginResult result = new OauthDto.LoginResult();
                    result.setSuccess(true);
                    result.setNeedBindPhone(false);
                    result.setToken(token);
                    result.setUser(buildUserMap(user));
                    return result;
                }
            }
        }

        // 5. 全新用户 → 生成 tempToken 引导绑定手机号
        String tempToken = UUID.randomUUID().toString().replace("-", "");
        String tempKey = TEMP_PREFIX + tempToken;

        // 存储第三方用户信息到 Redis
        Map<String, String> tempData = new HashMap<>();
        tempData.put("provider", request.getProvider());
        tempData.put("openId", openId);
        tempData.put("unionId", unionId != null ? unionId : "");
        tempData.put("nickname", nickname);
        tempData.put("avatarUrl", avatarUrl != null ? avatarUrl : "");
        tempData.put("accessToken", accessToken != null ? accessToken : "");

        redisTemplate.opsForHash().putAll(tempKey, tempData);
        redisTemplate.expire(tempKey, TEMP_TTL_MINUTES, TimeUnit.MINUTES);

        OauthDto.LoginResult result = new OauthDto.LoginResult();
        result.setSuccess(true);
        result.setNeedBindPhone(true);
        result.setTempToken(tempToken);
        result.setOauthNickname(nickname);
        result.setOauthAvatar(avatarUrl);
        return result;
    }

    @Override
    public OauthDto.LoginResult bindPhone(OauthDto.BindPhoneRequest request) {
        // 1. 从 Redis 取出临时第三方用户信息
        String tempKey = TEMP_PREFIX + request.getTempToken();
        Map<Object, Object> tempData = redisTemplate.opsForHash().entries(tempKey);

        if (tempData == null || tempData.isEmpty()) {
            return buildErrorResult("授权信息已过期，请重新登录");
        }

        // 2. 验证短信验证码
        String smsKey = "sms:code:" + request.getPhone();
        String storedCode = redisTemplate.opsForValue().get(smsKey);
        if (storedCode == null || !storedCode.equals(request.getSmsCode())) {
            return buildErrorResult("验证码错误或已过期");
        }
        redisTemplate.delete(smsKey);

        // 3. 创建新用户
        User newUser = new User();
        String userId = IdGenerator.toUserIdString(IdGenerator.generateId());
        newUser.setUserId(userId);
        newUser.setPhone(request.getPhone());
        newUser.setPassword(""); // 第三方登录用户无密码
        newUser.setNickname((String) tempData.get("nickname"));
        newUser.setAvatar((String) tempData.get("avatarUrl"));
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());
        userMapper.insert(newUser);

        // 4. 创建 OAuth 关联记录
        String provider = (String) tempData.get("provider");
        String openId = (String) tempData.get("openId");
        String unionId = (String) tempData.get("unionId");
        String nickname = (String) tempData.get("nickname");
        String avatarUrl = (String) tempData.get("avatarUrl");
        String accessToken = (String) tempData.get("accessToken");

        createOAuthRecord(userId, provider, openId, unionId, nickname, avatarUrl, accessToken, null);

        // 5. 创建钱包（新用户注册赠送）
        // 通过 UserService 或直接插入，此处简化为日志
        log.info("[OAuth] 新用户注册成功, userId={}, phone={}, provider={}", userId, request.getPhone(), provider);

        // 6. 清除临时数据
        redisTemplate.delete(tempKey);

        // 7. 生成 JWT token 返回
        String token = jwtUtil.generateToken(userId, request.getPhone());

        OauthDto.LoginResult result = new OauthDto.LoginResult();
        result.setSuccess(true);
        result.setNeedBindPhone(false);
        result.setToken(token);
        result.setUser(buildUserMap(newUser));
        return result;
    }

    @Override
    public boolean bindOAuthAccount(String userId, OauthDto.BindRequest request) {
        // 1. 用 code 换取用户信息（复用 callback 的逻辑）
        String stateKey = STATE_PREFIX + request.getState();
        String storedVerifier = redisTemplate.opsForValue().get(stateKey);
        if (storedVerifier == null) {
            return false;
        }
        redisTemplate.delete(stateKey);

        Map<String, Object> tokenResult = exchangeCodeForToken(request.getProvider(), request.getCode());
        if (tokenResult == null || tokenResult.containsKey("errcode")) {
            return false;
        }

        String openId = (String) tokenResult.get("openid");
        String unionId = (String) tokenResult.get("unionid");
        String accessToken = (String) tokenResult.get("access_token");

        Map<String, Object> userInfo = fetchUserInfo(request.getProvider(), accessToken, openId);
        String nickname = userInfo != null ? (String) userInfo.get("nickname") : "用户";
        String avatarUrl = userInfo != null ? (String) userInfo.get("headimgurl") : "";

        // 2. 检查是否已被其他用户绑定
        UserOAuth existing = userOAuthMapper.selectOne(
                new LambdaQueryWrapper<UserOAuth>()
                        .eq(UserOAuth::getProvider, request.getProvider())
                        .eq(UserOAuth::getOpenId, openId)
        );

        if (existing != null) {
            log.warn("[OAuth] 该第三方账号已被绑定, provider={}, openId={}", request.getProvider(), openId);
            return false;
        }

        // 3. 创建关联记录
        createOAuthRecord(userId, request.getProvider(), openId, unionId, nickname, avatarUrl, accessToken, tokenResult);
        return true;
    }

    @Override
    public boolean unbindOAuthAccount(String userId, String provider) {
        int deleted = userOAuthMapper.delete(
                new LambdaQueryWrapper<UserOAuth>()
                        .eq(UserOAuth::getUserId, userId)
                        .eq(UserOAuth::getProvider, provider)
        );
        return deleted > 0;
    }

    @Override
    public List<OauthDto.BoundAccountInfo> getBoundAccounts(String userId) {
        List<UserOAuth> oauthList = userOAuthMapper.selectList(
                new LambdaQueryWrapper<UserOAuth>()
                        .eq(UserOAuth::getUserId, userId)
        );

        List<OauthDto.BoundAccountInfo> result = new ArrayList<>();
        for (UserOAuth oauth : oauthList) {
            OauthDto.BoundAccountInfo info = new OauthDto.BoundAccountInfo();
            info.setProvider(oauth.getProvider());
            info.setNickname(oauth.getNickname());
            info.setAvatarUrl(oauth.getAvatarUrl());
            info.setBindTime(oauth.getCreateTime() != null ? oauth.getCreateTime().toString() : "");
            result.add(info);
        }
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 构造授权URL
     */
    private String buildAuthUrl(String provider, String state) {
        OAuthConfig.WechatConfig config;
        if ("wechat".equals(provider)) {
            config = (OAuthConfig.WechatConfig) oauthConfig.getWechat();
        } else {
            // QQ 使用相同的结构
            OAuthConfig.QqConfig qqConfig = oauthConfig.getQq();
            return qqConfig.getAuthUrl() +
                    "?state=" + state +
                    "&redirect_uri=" + qqConfig.getRedirectUri();
        }
        return config.getAuthUrl() +
                "?state=" + state +
                "&redirect_uri=" + config.getRedirectUri();
    }

    /**
     * 用 code 换取 access_token
     */
    private Map<String, Object> exchangeCodeForToken(String provider, String code) {
        try {
            String tokenUrl;
            if ("wechat".equals(provider)) {
                tokenUrl = oauthConfig.getWechat().getTokenUrl() +
                        "?code=" + code +
                        "&appid=" + oauthConfig.getWechat().getAppId() +
                        "&secret=" + oauthConfig.getWechat().getAppSecret() +
                        "&grant_type=authorization_code";
            } else {
                tokenUrl = oauthConfig.getQq().getTokenUrl() +
                        "?code=" + code +
                        "&appid=" + oauthConfig.getQq().getAppId() +
                        "&secret=" + oauthConfig.getQq().getAppSecret() +
                        "&grant_type=authorization_code";
            }

            ResponseEntity<Map> response = restTemplate.getForEntity(tokenUrl, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("[OAuth] code换取token异常, provider={}, code={}", provider, code, e);
            return null;
        }
    }

    /**
     * 获取第三方用户信息
     */
    private Map<String, Object> fetchUserInfo(String provider, String accessToken, String openId) {
        try {
            String userInfoUrl;
            if ("wechat".equals(provider)) {
                userInfoUrl = oauthConfig.getWechat().getUserInfoUrl() +
                        "?access_token=" + accessToken +
                        "&openid=" + openId;
            } else {
                userInfoUrl = oauthConfig.getQq().getUserInfoUrl() +
                        "?access_token=" + accessToken +
                        "&openid=" + openId;
            }

            ResponseEntity<Map> response = restTemplate.getForEntity(userInfoUrl, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("[OAuth] 获取用户信息异常, provider={}", provider, e);
            return null;
        }
    }

    /**
     * 创建 OAuth 关联记录
     */
    private void createOAuthRecord(String userId, String provider, String openId, String unionId,
                                   String nickname, String avatarUrl, String accessToken,
                                   Map<String, Object> tokenResult) {
        UserOAuth oauth = new UserOAuth();
        oauth.setId(IdGenerator.toOAuthIdString(IdGenerator.generateId()));
        oauth.setUserId(userId);
        oauth.setProvider(provider);
        oauth.setOpenId(openId);
        oauth.setUnionId(unionId);
        oauth.setNickname(nickname);
        oauth.setAvatarUrl(avatarUrl);
        oauth.setAccessToken(accessToken);
        if (tokenResult != null) {
            oauth.setRefreshToken((String) tokenResult.get("refresh_token"));
            Integer expiresIn = (Integer) tokenResult.get("expires_in");
            if (expiresIn != null) {
                oauth.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            }
        }
        oauth.setCreateTime(LocalDateTime.now());
        oauth.setUpdateTime(LocalDateTime.now());
        userOAuthMapper.insert(oauth);
    }

    /**
     * 更新 OAuth 的 token 信息
     */
    private void updateOAuthToken(UserOAuth oauth, String accessToken, Map<String, Object> tokenResult) {
        oauth.setAccessToken(accessToken);
        if (tokenResult != null) {
            oauth.setRefreshToken((String) tokenResult.get("refresh_token"));
            Integer expiresIn = (Integer) tokenResult.get("expires_in");
            if (expiresIn != null) {
                oauth.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            }
        }
        oauth.setUpdateTime(LocalDateTime.now());
        userOAuthMapper.updateById(oauth);
    }

    /**
     * 构建用户信息Map（返回给前端）
     */
    private Map<String, Object> buildUserMap(User user) {
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("userId", user.getUserId());
        userMap.put("phone", user.getPhone());
        userMap.put("nickname", user.getNickname());
        userMap.put("avatar", user.getAvatar());
        userMap.put("email", user.getEmail());
        return userMap;
    }

    /**
     * 构建错误结果
     */
    private OauthDto.LoginResult buildErrorResult(String message) {
        OauthDto.LoginResult result = new OauthDto.LoginResult();
        result.setSuccess(false);
        result.setNeedBindPhone(false);
        return result;
    }
}
