package com.xx.jaseatschoicejava;

import com.xx.jaseatschoicejava.util.IdGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试ID生成器
 */
class IdGeneratorTest {

    @Test
    void testIdGeneration() {
        // 测试生成10个ID
        for (int i = 0; i < 10; i++) {
            Long id = IdGenerator.generateId();
            System.out.println("生成的ID: " + id);
            // 验证ID在格式化后是16位数字
            assertEquals(16, String.format("%016d", id).length());
        }
    }

    @Test
    void testIdConversion() {
        // 测试用户ID转换
        Long userId = 1234567890123456L;
        String userPrefixedId = IdGenerator.toUserIdString(userId);
        System.out.println("用户ID转换: " + userId + " -> " + userPrefixedId);
        assertEquals("U1234567890123456", userPrefixedId);

        // 测试商家ID转换
        Long merchantId = 9876543210987654L;
        String merchantPrefixedId = IdGenerator.toMerchantIdString(merchantId);
        System.out.println("商家ID转换: " + merchantId + " -> " + merchantPrefixedId);
        assertEquals("M9876543210987654", merchantPrefixedId);

        // 测试群ID转换
        Long groupId = 5555666677778888L;
        String groupPrefixedId = IdGenerator.toGroupIdString(groupId);
        System.out.println("群ID转换: " + groupId + " -> " + groupPrefixedId);
        assertEquals("G5555666677778888", groupPrefixedId);
    }

    @Test
    void testIdExtraction() {
        // 测试从带前缀的ID中提取原始ID
        String userPrefixedId = "U1234567890123456";
        Long userId = IdGenerator.toLongId(userPrefixedId);
        System.out.println("提取用户ID: " + userPrefixedId + " -> " + userId);
        assertEquals(1234567890123456L, userId);

        // 测试从带前缀的ID中提取类型
        String idType = IdGenerator.extractIdType(userPrefixedId);
        System.out.println("提取ID类型: " + userPrefixedId + " -> " + idType);
        assertEquals("USER", idType);
    }

    @Test
    void testIdValidation() {
        // 测试有效ID
        assertTrue(IdGenerator.isValidId("U1234567890123456"));
        assertTrue(IdGenerator.isValidId("M9876543210987654"));
        assertTrue(IdGenerator.isValidId("G5555666677778888"));

        // 测试无效ID
        assertFalse(IdGenerator.isValidId(""));
        assertFalse(IdGenerator.isValidId("A1234567890123456")); // 无效前缀
        assertFalse(IdGenerator.isValidId("U123456789012345")); // 长度不足
        assertFalse(IdGenerator.isValidId("U123456789012345a")); // 包含非数字字符
    }
}
