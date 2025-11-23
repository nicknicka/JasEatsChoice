package com.xx.jaseatschoicejava.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    // Use a simple secret key string for simplicity (in production, use a secure key management system)
    // Use a simple secret key string for simplicity (in production, use a secure key management system)
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor("jaseatschoice_secret_key_123456789012345678901234567890".getBytes(StandardCharsets.UTF_8));

    // Token expiration time (1 hour)
    private static final long EXPIRATION_TIME = 3600000;

    /**
     * Generate JWT token
     * @param userId User ID
     * @param phone User phone number
     * @return JWT token
     */
    public static String generateToken(Long userId, String phone) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("phone", phone);

        return Jwts.builder()
                .claims(claims)
                .subject(phone)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Extract claims from JWT token
     * @param token JWT token
     * @return Claims
     */
    public static Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getBody();
    }

    /**
     * Extract user ID from JWT token
     * @param token JWT token
     * @return User ID
     */
    public static Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    /**
     * Extract phone number from JWT token
     * @param token JWT token
     * @return Phone number
     */
    public static String extractPhone(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Check if JWT token is expired
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * Validate JWT token
     * @param token JWT token
     * @param phone Phone number
     * @return true if valid, false otherwise
     */
    public static boolean validateToken(String token, String phone) {
        return (extractPhone(token).equals(phone) && !isTokenExpired(token));
    }
}
