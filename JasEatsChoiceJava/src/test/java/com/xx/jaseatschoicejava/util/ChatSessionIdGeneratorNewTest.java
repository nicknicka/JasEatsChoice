package com.xx.jaseatschoicejava.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatSessionIdGenerator 测试类
 * 测试使用 IdGenerator 生成会话ID
 *
 * @author xx
 * @date 2026-01-20
 */
class ChatSessionIdGeneratorNewTest {

    private static final String USER1 = "U1234567890123456";
    private static final String USER2 = "U9876543210987654";
    private static final String GROUP_ID = "G1234567890123456";

    /**
     * 测试使用IdGenerator生成单聊会话ID
     */
    @Test
    void testGenerateSingleChatSessionIdWithIdGenerator() {
        System.out.println("\n========== 测试 IdGenerator 生成单聊会话ID ==========");

        // 生成第一个会话ID
        String sessionId1 = ChatSessionIdGenerator.generateSingleChatSessionIdWithIdGenerator(USER1, USER2);
        System.out.println("用户1 + 用户2: " + sessionId1);

        // 验证格式
        assertNotNull(sessionId1);
        assertTrue(sessionId1.startsWith("S"), "应以S开头");
        assertTrue(sessionId1.length() == 17, "长度应为17字符（S + 16位数字）");

        // 生成第二个会话ID（相同用户）
        String sessionId2 = ChatSessionIdGenerator.generateSingleChatSessionIdWithIdGenerator(USER2, USER1);
        System.out.println("用户2 + 用户1: " + sessionId2);

        // 验证：每次生成不同的ID（无序、随机）
        assertNotEquals(sessionId1, sessionId2, "相同用户应对应生成不同的会话ID（随机性）");

        // 验证：都是S开头的17位字符
        assertTrue(sessionId2.startsWith("S"));
        assertTrue(sessionId2.length() == 17);

        // 生成第三个会话ID（不同用户）
        String sessionId3 = ChatSessionIdGenerator.generateSingleChatSessionIdWithIdGenerator(USER1, "U1111111111111111");
        System.out.println("用户1 + 用户3: " + sessionId3);

        assertNotEquals(sessionId1, sessionId3, "不同用户对应应生成不同的会话ID");

        System.out.println("✅ IdGenerator 单聊会话ID测试通过");
    }

    /**
     * 测试生成多个会话ID，验证随机性和唯一性
     */
    @Test
    void testGenerateMultipleSessionIds() {
        System.out.println("\n========== 测试批量生成会话ID ==========");

        int count = 1000;
        java.util.Set<String> uniqueIds = new java.util.HashSet<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            String sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithIdGenerator(USER1, USER2);
            uniqueIds.add(sessionId);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("生成 " + count + " 个会话ID");
        System.out.println("唯一ID数量: " + uniqueIds.size());
        System.out.println("耗时: " + duration + "ms");
        System.out.println("平均每个ID: " + (duration * 1.0 / count) + "ms");

        // 验证：所有ID都是唯一的
        assertEquals(count, uniqueIds.size(), "所有生成的ID都应该是唯一的");

        // 验证：性能应该足够好（每个ID应该在10ms内生成）
        assertTrue(duration < count * 10, "生成速度应该足够快");

        System.out.println("✅ 批量生成测试通过");
    }

    /**
     * 测试群聊会话ID
     */
    @Test
    void testGroupChatSessionId() {
        System.out.println("\n========== 测试群聊会话ID ==========");

        String sessionId = ChatSessionIdGenerator.getGroupChatSessionId(GROUP_ID);
        System.out.println("群聊session_id: " + sessionId);

        // 验证
        assertNotNull(sessionId);
        assertEquals(GROUP_ID, sessionId, "群聊应直接使用群ID");

        System.out.println("✅ 群聊session_id测试通过");
    }

    /**
     * 测试根据消息类型生成会话ID
     */
    @Test
    void testGenerateSessionIdByType() {
        System.out.println("\n========== 测试根据消息类型生成session_id ==========");

        // 单聊
        String singleSessionId = ChatSessionIdGenerator.generateSessionId("single", USER1, USER2);
        System.out.println("单聊session_id: " + singleSessionId);

        assertTrue(singleSessionId.startsWith("S"));
        assertTrue(singleSessionId.length() == 17);

        // 群聊
        String groupSessionId = ChatSessionIdGenerator.generateSessionId("group", USER1, GROUP_ID);
        System.out.println("群聊session_id: " + groupSessionId);

        assertEquals(GROUP_ID, groupSessionId);

        System.out.println("✅ 根据消息类型生成session_id测试通过");
    }

    /**
     * 测试会话ID验证
     */
    @Test
    void testIsValidSessionId() {
        System.out.println("\n========== 测试session_id验证 ==========");

        // 有效的session_id
        String validSessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithIdGenerator(USER1, USER2);
        assertTrue(ChatSessionIdGenerator.isValidSessionId(validSessionId), "生成的session_id应该有效");
        System.out.println("有效session_id: " + validSessionId);

        // 无效的session_id
        assertFalse(ChatSessionIdGenerator.isValidSessionId(null), "null应该是无效的");
        assertFalse(ChatSessionIdGenerator.isValidSessionId(""), "空字符串应该是无效的");
        assertFalse(ChatSessionIdGenerator.isValidSessionId("   "), "空白字符串应该是无效的");

        System.out.println("✅ session_id验证测试通过");
    }

    /**
     * 测试无序性
     */
    @Test
    void testUnorderedness() {
        System.out.println("\n========== 测试会话ID无序性 ==========");

        java.util.List<String> sessionIds = new java.util.ArrayList<>();

        // 生成10个会话ID
        for (int i = 0; i < 10; i++) {
            String sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithIdGenerator(USER1, USER2);
            sessionIds.add(sessionId);
            System.out.println((i + 1) + ": " + sessionId);
        }

        // 验证：它们应该是不同的
        java.util.Set<String> uniqueSet = new java.util.HashSet<>(sessionIds);
        assertEquals(10, uniqueSet.size(), "所有会话ID都应该是唯一的");

        // 验证：提取数字部分，检查是否无序
        java.util.List<Long> numbers = new java.util.ArrayList<>();
        for (String sessionId : sessionIds) {
            String numStr = sessionId.substring(1); // 去掉S前缀
            numbers.add(Long.parseLong(numStr));
        }

        // 检查数字是否是递增的（如果是有序的，应该大致递增）
        boolean isOrdered = true;
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) <= numbers.get(i - 1)) {
                isOrdered = false;
                break;
            }
        }

        System.out.println("是否严格递增: " + isOrdered);
        // 由于IdGenerator使用了随机打乱，不应该是严格递增的
        assertFalse(isOrdered, "会话ID不应该是有序的");

        System.out.println("✅ 无序性测试通过");
    }

    /**
     * 性能测试
     */
    @Test
    void testPerformance() {
        System.out.println("\n========== 性能测试 ==========");

        int count = 10000;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            ChatSessionIdGenerator.generateSingleChatSessionIdWithIdGenerator(USER1, USER2);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("生成 " + count + " 个会话ID");
        System.out.println("总耗时: " + duration + "ms");
        System.out.println("平均每个: " + (duration * 1.0 / count) + "ms");
        System.out.println("每秒可生成: " + (count * 1000.0 / duration) + " 个");

        // 性能应该足够好（10000个应该在10秒内完成）
        assertTrue(duration < 10000, "性能应该足够好");

        System.out.println("✅ 性能测试通过");
    }
}
