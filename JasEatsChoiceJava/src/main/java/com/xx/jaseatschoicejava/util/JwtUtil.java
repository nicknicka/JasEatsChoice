package com.xx.jaseatschoicejava.util;

import com.xx.jaseatschoicejava.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT工具类
 * 使用Spring配置，支持从配置文件读取密钥和过期时间
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
@Component
public class JwtUtil {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * Generate JWT token
     * @param userId User ID
     * @param phone User phone number
     * @return JWT token
     */
    public String generateToken(String userId, String phone) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("phone", phone);

        return Jwts.builder()
                .claims(claims)
                .subject(phone)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(jwtConfig.getSigningKey())
                .compact();
    }

    /**
     * Generate JWT token with permissions (for admin)
     * @param userId User ID
     * @param phone User phone number
     * @param permissions Permission list
     * @return JWT token
     */
    public String generateToken(String userId, String phone, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("phone", phone);
        claims.put("permissions", permissions);

        return Jwts.builder()
                .claims(claims)
                .subject(phone)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(jwtConfig.getSigningKey())
                .compact();
    }

    /**
     * Extract claims from JWT token
     * @param token JWT token
     * @return Claims
     */
    @SuppressWarnings("deprecation")
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtConfig.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getBody();
    }

    /**
     * Extract user ID from JWT token
     * @param token JWT token
     * @return User ID
     */
    public String extractUserId(String token) {
        return extractClaims(token).get("userId", String.class);
    }

    /**
     * Extract phone number from JWT token
     * @param token JWT token
     * @return Phone number
     */
    public String extractPhone(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract permissions from JWT token
     * @param token JWT token
     * @return List of permissions
     */
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        Object permissions = extractClaims(token).get("permissions");
        if (permissions instanceof List) {
            return (List<String>) permissions;
        }
        return null;
    }

    /**
     * Check if JWT token is expired
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * Validate JWT token
     * @param token JWT token
     * @param phone Phone number
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token, String phone) {
        return (extractPhone(token).equals(phone) && !isTokenExpired(token));
    }
}
