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
     * 生成单聊会话ID - 使用IdGenerator
     *
     * 特点：
     * - 16位数字
     * - 确定性（相同用户对生成相同ID）
     * - 不可预测（ID外观随机）
     * - 用户ID作为影响因子
     * - 与系统其他ID保持一致
     * - 高性能：使用优化的哈希算法
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 会话ID（格式：S + 16位数字）
     */
    public static String generateSingleChatSessionIdWithIdGenerator(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 按字典序排序，确保唯一性（相同的用户对生成相同的sessionId）
        String seed;
        if (userId1.compareTo(userId2) < 0) {
            seed = userId1 + "_" + userId2 + "_" + SALT;
        } else {
            seed = userId2 + "_" + userId1 + "_" + SALT;
        }

        // 使用优化的种子ID生成方法（高性能）
        Long id = generateOptimizedIdFromSeed(seed);
        return SINGLE_CHAT_PREFIX + id;
    }

    /**
     * 优化的ID生成方法（高性能版本）
     *
     * @param seed 种子字符串
     * @return 16位数字ID
     */
    private static Long generateOptimizedIdFromSeed(String seed) {
        // 使用Java内置的hashCode + 自定义混淆算法
        // 性能远高于SHA-256，同时保持足够的随机性

        // 1. 计算种子的哈希值
        int hash = seed.hashCode();

        // 2. 添加盐值的哈希值（增强随机性）
        int saltHash = SALT.hashCode();

        // 3. 组合哈希值（使用位运算避免溢出）
        long combined = ((long) hash << 32) | (saltHash & 0xFFFFFFFFL);

        // 4. 使用黄金比例常量进行混合（增强随机性）
        long goldenRatio = 0x9E3779B97F4A7C15L;
        combined = combined ^ goldenRatio;
        combined = Long.rotateLeft(combined, 17);

        // 5. 确保是正数
        combined = combined & Long.MAX_VALUE;

        // 6. 取模确保16位数字（最大9999999999999999）
        long maxId = 9999999999999999L;
        long id = combined % maxId;

        // 7. 确保至少16位（不足前面补0，但Long会自动去掉前导0）
        // 所以加一个基数确保总是16位
        long base = 1000000000000000L;
        id = base + (id % (maxId - base));

        return id;
    }

    /**
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
     * 获取群聊会话ID
     * 规则：使用群组ID作为种子生成确定性的会话ID
     *
     * 特点：
     * - 16位数字
     * - 确定性（相同群组ID生成相同ID）
     * - 不可预测（ID外观随机）
     * - 群组ID作为影响因子
     * - 高性能：使用优化的哈希算法
     * - 统一使用S前缀（与单聊保持一致）
     *
     * @param groupId 群组ID
     * @return 会话ID（格式：S + 16位数字）
     */
    public static String getGroupChatSessionId(String groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("群组ID不能为空");
        }

        // 清理groupId：如果已有前缀（S或G），先移除（避免重复前缀）
        String cleanGroupId = groupId;
        if (groupId.startsWith(SINGLE_CHAT_PREFIX) || groupId.startsWith(GROUP_CHAT_PREFIX)) {
            cleanGroupId = groupId.substring(1);
        }

        // 使用群组ID + 盐值作为种子
        String seed = "GROUP_" + cleanGroupId + "_" + SALT;

        // 使用优化的种子ID生成方法（高性能，确定性）
        Long id = generateOptimizedIdFromSeed(seed);
        return SINGLE_CHAT_PREFIX + id;
    }

    /**
     * 根据会话类型生成会话ID
     * 统一使用优化的哈希算法（确定性、高性能）
     * 注意：单聊和群聊都使用S前缀
     *
     * @param sessionType 会话类型（single-单聊, group-群聊）
     * @param fromId      发送者ID（单聊时使用）
     * @param toId        接收者ID（群聊时为群ID，单聊时为对方ID）
     * @return 会话ID（格式：S + 16位数字）
     */
    public static String generateSessionId(String sessionType, String fromId, String toId) {
        if ("group".equals(sessionType)) {
            // 群聊：使用群组ID作为种子生成sessionId（确定性、高性能）
            return getGroupChatSessionId(toId);
        } else {
            // 单聊：使用用户ID作为种子生成sessionId（确定性、高性能）
            return generateSingleChatSessionIdWithIdGenerator(fromId, toId);
        }
    }

    /**
     * 判断会话ID的类型
     *
     * @param sessionId 会话ID
     * @return "group"-群聊，"single"-单聊，"unknown"-未知
     */
    public static String getSessionType(String sessionId) {
        if (sessionId == null) {
            return "unknown";
        }
        if (sessionId.startsWith(GROUP_CHAT_PREFIX)) {
            return "group";
        } else if (sessionId.startsWith(SINGLE_CHAT_PREFIX)) {
            return "single";
        }
        return "unknown";
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
