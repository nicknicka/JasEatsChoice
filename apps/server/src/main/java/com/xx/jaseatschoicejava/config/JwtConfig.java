package com.xx.jaseatschoicejava.config;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * JWT配置类
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
@Configuration
public class JwtConfig {

    /**
     * JWT密钥（从配置文件读取）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT过期时间（从配置文件读取）
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 获取签名密钥
     */
    public Key getSigningKey() {
        return new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            SignatureAlgorithm.HS256.getJcaName()
        );
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
