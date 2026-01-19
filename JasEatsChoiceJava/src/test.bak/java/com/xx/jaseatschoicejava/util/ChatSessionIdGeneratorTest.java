package com.xx.jaseatschoicejava.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatSessionIdGenerator 测试类
 *
 * @author xx
 * @date 2026-01-19
 */
public class ChatSessionIdGeneratorTest {

    // 测试用户ID
    private static final String USER1 = "3384650106421960";
    private static final String USER2 = "1000000000000000";
    private static final String USER3 = "1000000000000002";

    /**
     * 测试单聊会话ID生成（MD5哈希）
     */
    @Test
    public void testGenerateSingleChatSessionId() {
        System.out.println("\n========== 测试MD5哈希方案 ==========");

        // 测试1：生成session_id
        String sessionId1 = ChatSessionIdGenerator.generateSingleChatSessionId(USER1, USER2);
        System.out.println("user1 + user2: " + sessionId1);

        // 验证格式
        assertNotNull(sessionId1);
        assertTrue(sessionId1.startsWith("S"), "应以S开头");
        assertTrue(sessionId1.length() == 33, "长度应为33字符（S + 32位MD5）");

        // 测试2：验证唯一性（相同输入应生成相同输出）
        String sessionId2 = ChatSessionIdGenerator.generateSingleChatSessionId(USER2, USER1);
        System.out.println("user2 + user1: " + sessionId2);

        assertEquals(sessionId1, sessionId2, "相同用户对应生成相同的session_id");

        // 测试3：不同用户对应生成不同的session_id
        String sessionId3 = ChatSessionIdGenerator.generateSingleChatSessionId(USER1, USER3);
        System.out.println("user1 + user3: " + sessionId3);

        assertNotEquals(sessionId1, sessionId3, "不同用户对应生成不同的session_id");

        System.out.println("✅ MD5哈希方案测试通过");
    }

    /**
     * 测试SHA-256哈希方案
     */
    @Test
    public void testGenerateSingleChatSessionIdWithSHA256() {
        System.out.println("\n========== 测试SHA-256哈希方案 ==========");

        String sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithSHA256(USER1, USER2);
        System.out.println("SHA-256: " + sessionId);

        // 验证格式
        assertNotNull(sessionId);
        assertTrue(sessionId.startsWith("S"), "应以S开头");
        assertTrue(sessionId.length() == 65, "长度应为65字符（S + 64位SHA-256）");

        // 验证唯一性
        String sessionId2 = ChatSessionIdGenerator.generateSingleChatSessionIdWithSHA256(USER2, USER1);
        assertEquals(sessionId, sessionId2, "相同用户对应生成相同的session_id");

        System.out.println("✅ SHA-256哈希方案测试通过");
    }

    /**
     * 测试UUID命名空间方案
     */
    @Test
    public void testGenerateSingleChatSessionIdWithUUID() {
        System.out.println("\n========== 测试UUID命名空间方案 ==========");

        String sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithUUID(USER1, USER2);
        System.out.println("UUID: " + sessionId);

        // 验证格式
        assertNotNull(sessionId);
        assertTrue(sessionId.startsWith("S"), "应以S开头");

        // 验证唯一性
        String sessionId2 = ChatSessionIdGenerator.generateSingleChatSessionIdWithUUID(USER2, USER1);
        assertEquals(sessionId, sessionId2, "相同用户对应生成相同的session_id");

        System.out.println("✅ UUID命名空间方案测试通过");
    }

    /**
     * 测试群聊会话ID生成
     */
    @Test
    public void testGetGroupChatSessionId() {
        System.out.println("\n========== 测试群聊会话ID ==========");

        String groupId = "G1234567890123456";
        String sessionId = ChatSessionIdGenerator.getGroupChatSessionId(groupId);

        System.out.println("群聊session_id: " + sessionId);

        // 验证
        assertNotNull(sessionId);
        assertEquals(groupId, sessionId, "群聊应直接使用群ID");

        System.out.println("✅ 群聊session_id测试通过");
    }

    /**
     * 测试根据消息类型生成会话ID
     */
    @Test
    public void testGenerateSessionId() {
        System.out.println("\n========== 测试根据消息类型生成session_id ==========");

        // 测试单聊
        String singleSessionId = ChatSessionIdGenerator.generateSessionId("single", USER1, USER2);
        System.out.println("单聊: " + singleSessionId);
        assertNotNull(singleSessionId);
        assertTrue(singleSessionId.startsWith("S"));

        // 测试群聊
        String groupSessionId = ChatSessionIdGenerator.generateSessionId("group", USER1, "G1234567890123456");
        System.out.println("群聊: " + groupSessionId);
        assertNotNull(groupSessionId);
        assertEquals("G1234567890123456", groupSessionId);

        System.out.println("✅ 根据消息类型生成session_id测试通过");
    }

    /**
     * 测试会话ID验证
     */
    @Test
    public void testIsValidSessionId() {
        System.out.println("\n========== 测试session_id验证 ==========");

        // 有效ID
        assertTrue(ChatSessionIdGenerator.isValidSessionId("Sabc123"));
        assertTrue(ChatSessionIdGenerator.isValidSessionId("G123456"));

        // 无效ID
        assertFalse(ChatSessionIdGenerator.isValidSessionId(null));
        assertFalse(ChatSessionIdGenerator.isValidSessionId(""));
        assertFalse(ChatSessionIdGenerator.isValidSessionId("   "));

        System.out.println("✅ session_id验证测试通过");
    }

    /**
     * 测试session_id升级
     */
    @Test
    public void testUpgradeSessionId() {
        System.out.println("\n========== 测试session_id升级 ==========");

        String oldSessionId = USER1 + "_" + USER2;
        System.out.println("旧格式: " + oldSessionId);

        String newSessionId = ChatSessionIdGenerator.upgradeSessionId(oldSessionId);
        System.out.println("新格式: " + newSessionId);

        // 验证升级后的格式
        assertNotNull(newSessionId);
        assertTrue(newSessionId.startsWith("S"), "升级后应以S开头");
        assertNotEquals(oldSessionId, newSessionId, "升级后应与旧格式不同");

        // 验证与直接生成的一致性
        String directSessionId = ChatSessionIdGenerator.generateSingleChatSessionId(USER1, USER2);
        assertEquals(newSessionId, directSessionId, "升级结果应与直接生成一致");

        System.out.println("✅ session_id升级测试通过");
    }

    /**
     * 性能测试
     */
    @Test
    public void testPerformance() {
        System.out.println("\n========== 性能测试 ==========");

        int iterations = 10000;

        // 测试MD5性能
        long md5Start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ChatSessionIdGenerator.generateSingleChatSessionId(USER1, USER2);
        }
        long md5Time = (System.nanoTime() - md5Start) / 1000000;
        System.out.println("MD5哈希 (" + iterations + "次): " + md5Time + "ms");

        // 测试SHA-256性能
        long sha256Start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ChatSessionIdGenerator.generateSingleChatSessionIdWithSHA256(USER1, USER2);
        }
        long sha256Time = (System.nanoTime() - sha256Start) / 1000000;
        System.out.println("SHA-256哈希 (" + iterations + "次): " + sha256Time + "ms");

        // 测试UUID性能
        long uuidStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ChatSessionIdGenerator.generateSingleChatSessionIdWithUUID(USER1, USER2);
        }
        long uuidTime = (System.nanoTime() - uuidStart) / 1000000;
        System.out.println("UUID命名 (" + iterations + "次): " + uuidTime + "ms");

        System.out.println("\n性能排名：");
        System.out.println("1. MD5: " + md5Time + "ms ⭐ 最快");
        System.out.println("2. SHA-256: " + sha256Time + "ms");
        System.out.println("3. UUID: " + uuidTime + "ms");

        System.out.println("✅ 性能测试完成");
    }

    /**
     * 验证不可逆性
     */
    @Test
    public void testIrreversibility() {
        System.out.println("\n========== 测试不可逆性 ==========");

        String sessionId = ChatSessionIdGenerator.generateSingleChatSessionId(USER1, USER2);
        System.out.println("生成的session_id: " + sessionId);

        // 验证：session_id不包含原始用户ID
        assertFalse(sessionId.contains(USER1), "不应包含原始用户ID");
        assertFalse(sessionId.contains(USER2), "不应包含原始用户ID");

        // 验证：无法通过字符串操作反推
        assertNotEquals(sessionId, USER1 + "_" + USER2);
        assertNotEquals(sessionId, USER2 + "_" + USER1);

        System.out.println("✅ 不可逆性验证通过（用户ID被隐藏）");
    }

    /**
     * 主方法：手动测试
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  ChatSessionIdGenerator 测试");
        System.out.println("========================================");

        ChatSessionIdGeneratorTest test = new ChatSessionIdGeneratorTest();

        // 运行所有测试
        test.testGenerateSingleChatSessionId();
        test.testGenerateSingleChatSessionIdWithSHA256();
        test.testGenerateSingleChatSessionIdWithUUID();
        test.testGetGroupChatSessionId();
        test.testGenerateSessionId();
        test.testIsValidSessionId();
        test.testUpgradeSessionId();
        test.testPerformance();
        test.testIrreversibility();

        System.out.println("\n========================================");
        System.out.println("  所有测试完成！✅");
        System.out.println("========================================");
    }
}
