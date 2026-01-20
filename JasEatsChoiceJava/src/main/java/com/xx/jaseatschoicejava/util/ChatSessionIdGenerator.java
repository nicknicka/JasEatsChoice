package com.xx.jaseatschoicejava.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 聊天会话ID生成工具类
 *
 * 提供多种会话ID生成策略：
 * 1. IdGenerator（推荐）：无序、不可预测、16位数字
 * 2. MD5哈希：快速、安全、固定长度
 * 3. SHA-256哈希：更安全、但较长
 * 4. UUID命名：标准格式、可扩展
 *
 * @author xx
 * @date 2026-01-19
 */
public class ChatSessionIdGenerator {

    // 哈希算法常量
    private static final String HASH_ALGORITHM_MD5 = "MD5";
    private static final String HASH_ALGORITHM_SHA256 = "SHA-256";

    // 会话前缀
    private static final String SINGLE_CHAT_PREFIX = "S";  // 单聊前缀
    private static final String GROUP_CHAT_PREFIX = "G";   // 群聊前缀

    // 盐值（可选，用于增强安全性）
    private static final String SALT = "JasEatsChoice_Chat_2026";

    /**
     * ========== 推荐方案：使用IdGenerator（无序、不可预测） ==========
     * 生成单聊会话ID - 使用IdGenerator
     *
     * 特点：
     * - 16位数字
     * - 完全无序、不可预测
     * - 高碰撞安全性
     * - 与系统其他ID保持一致
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 会话ID（格式：S + 16位数字）
     */
    public static String generateSingleChatSessionIdWithIdGenerator(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 使用IdGenerator生成16位随机数字ID
        Long id = IdGenerator.generateId();
        return SINGLE_CHAT_PREFIX + id;
    }

    /**
     * ========== 方案1：MD5哈希（推荐） ==========
     * 生成单聊会话ID - 使用MD5哈希
     *
     * 特点：
     * - 固定32字符长度
     * - 不可逆（保护用户隐私）
     * - 相同输入总是生成相同输出
     * - 性能优秀
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 会话ID（格式：S + 32位MD5哈希）
     */
    public static String generateSingleChatSessionId(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 按字典序排序，确保唯一性
        String sorted;
        if (userId1.compareTo(userId2) < 0) {
            sorted = userId1 + "_" + userId2;
        } else {
            sorted = userId2 + "_" + userId1;
        }

        // 添加盐值并生成MD5哈希
        String hash = md5Hash(sorted + SALT);
        return SINGLE_CHAT_PREFIX + hash;
    }

    /**
     * ========== 方案2：SHA-256哈希（更安全） ==========
     * 生成单聊会话ID - 使用SHA-256哈希
     *
     * 特点：
     * - 固定64字符长度
     * - 更安全的哈希算法
     * - 不可逆
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 会话ID（格式：S + 64位SHA-256哈希）
     */
    public static String generateSingleChatSessionIdWithSHA256(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 按字典序排序
        String sorted;
        if (userId1.compareTo(userId2) < 0) {
            sorted = userId1 + "_" + userId2;
        } else {
            sorted = userId2 + "_" + userId1;
        }

        // 生成SHA-256哈希
        String hash = sha256Hash(sorted + SALT);
        return SINGLE_CHAT_PREFIX + hash;
    }

    /**
     * ========== 方案3：UUID命名空间（标准格式） ==========
     * 生成单聊会话ID - 使用UUID v5
     *
     * 特点：
     * - 标准UUID格式（36字符）
     * - 基于命名空间
     * - 可扩展性好
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 会话ID（UUID格式）
     */
    public static String generateSingleChatSessionIdWithUUID(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 按字典序排序
        String sorted;
        if (userId1.compareTo(userId2) < 0) {
            sorted = userId1 + "_" + userId2;
        } else {
            sorted = userId2 + "_" + userId1;
        }

        // 使用UUID v5生成（基于命名空间的UUID）
        UUID namespace = UUID.nameUUIDFromBytes((SALT + sorted).getBytes(StandardCharsets.UTF_8));
        return SINGLE_CHAT_PREFIX + namespace.toString().replace("-", "");
    }

    /**
     * 获取群聊会话ID
     * 规则：直接使用群组ID（群聊ID已经有规范的格式）
     *
     * @param groupId 群组ID
     * @return 会话ID（群组ID）
     */
    public static String getGroupChatSessionId(String groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("群组ID不能为空");
        }
        return groupId;
    }

    /**
     * 根据消息类型生成会话ID
     * 默认使用IdGenerator方案（推荐：无序、不可预测）
     *
     * @param msgType 消息类型
     * @param fromId  发送者ID
     * @param toId    接收者ID
     * @return 会话ID
     */
    public static String generateSessionId(String msgType, String fromId, String toId) {
        if ("group".equals(msgType)) {
            return getGroupChatSessionId(toId);
        } else {
            // 使用IdGenerator生成单聊会话ID（无序、不可预测）
            return generateSingleChatSessionIdWithIdGenerator(fromId, toId);
        }
    }

    /**
     * 验证会话ID是否有效
     *
     * @param sessionId 会话ID
     * @return true-有效，false-无效
     */
    public static boolean isValidSessionId(String sessionId) {
        return sessionId != null && !sessionId.trim().isEmpty();
    }

    /**
     * 生成MD5哈希
     *
     * @param input 输入字符串
     * @return MD5哈希值（32位小写十六进制）
     */
    private static String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM_MD5);
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 生成SHA-256哈希
     *
     * @param input 输入字符串
     * @return SHA-256哈希值（64位小写十六进制）
     */
    private static String sha256Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM_SHA256);
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * 字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串（小写）
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // ========== 便捷方法 ==========

    /**
     * 从旧的简单格式升级到新的哈希格式
     *
     * @param oldSessionId 旧的会话ID（格式：user1_user2）
     * @return 新的会话ID（MD5哈希格式）
     */
    public static String upgradeSessionId(String oldSessionId) {
        if (oldSessionId == null || !oldSessionId.contains("_")) {
            return oldSessionId;
        }

        String[] parts = oldSessionId.split("_");
        if (parts.length == 2) {
            return generateSingleChatSessionId(parts[0], parts[1]);
        }
        return oldSessionId;
    }
}
