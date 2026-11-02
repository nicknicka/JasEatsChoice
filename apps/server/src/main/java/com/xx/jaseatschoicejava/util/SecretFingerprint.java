package com.xx.jaseatschoicejava.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 密钥不可逆指纹：只暴露短哈希前缀，用于管理端确认当前生效的是哪一份密钥。
 */
public final class SecretFingerprint {

    private static final int PREFIX_BYTES = 6;

    private SecretFingerprint() {
    }

    public static String shortSha256(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, PREFIX_BYTES);
        }
        catch (Exception exception) {
            return "unavailable";
        }
    }
}
