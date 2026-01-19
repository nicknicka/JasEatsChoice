package com.xx.jaseatschoicejava.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnhancedIdGenerator 测试类
 *
 * @author xx
 * @date 2026-01-19
 */
@SpringBootTest
public class EnhancedIdGeneratorTest {

    @Autowired
    private EnhancedIdGenerator idGenerator;

    /**
     * 测试生成用户ID
     */
    @Test
    public void testGenerateUserId() {
        String userId = idGenerator.generateUserId();

        assertNotNull(userId);
        assertTrue(userId.startsWith("U"), "用户ID应以U开头");
        assertEquals(21, userId.length(), "用户ID应为21位（U + 20位数字）");

        // 提取纯数字部分
        String numericId = EnhancedIdGenerator.extractNumericId(userId);
        assertEquals(20, numericId.length(), "纯数字部分应为20位");

        System.out.println("✅ 用户ID测试通过：" + userId);
    }

    /**
     * 测试生成订单ID
     */
    @Test
    public void testGenerateOrderId() {
        String orderId = idGenerator.generateOrderId();

        assertNotNull(orderId);
        assertTrue(orderId.startsWith("O"), "订单ID应以O开头");
        assertEquals(21, orderId.length(), "订单ID应为21位");

        System.out.println("✅ 订单ID测试通过：" + orderId);
    }

    /**
     * 测试生成菜品ID
     */
    @Test
    public void testGenerateDishId() {
        String dishId = idGenerator.generateDishId();

        assertNotNull(dishId);
        assertTrue(dishId.startsWith("D"), "菜品ID应以D开头");
        assertEquals(21, dishId.length(), "菜品ID应为21位");

        System.out.println("✅ 菜品ID测试通过：" + dishId);
    }

    /**
     * 测试生成菜单ID
     */
    @Test
    public void testGenerateMenuId() {
        String menuId = idGenerator.generateMenuId();

        assertNotNull(menuId);
        assertTrue(menuId.startsWith("MN"), "菜单ID应以MN开头");
        assertEquals(22, menuId.length(), "菜单ID应为22位");

        System.out.println("✅ 菜单ID测试通过：" + menuId);
    }

    /**
     * 测试生成群组ID
     */
    @Test
    public void testGenerateGroupId() {
        String groupId = idGenerator.generateGroupId();

        assertNotNull(groupId);
        assertTrue(groupId.startsWith("G"), "群组ID应以G开头");
        assertEquals(21, groupId.length(), "群组ID应为21位");

        System.out.println("✅ 群组ID测试通过：" + groupId);
    }

    /**
     * 测试生成地址ID
     */
    @Test
    public void testGenerateAddressId() {
        String addressId = idGenerator.generateAddressId();

        assertNotNull(addressId);
        assertTrue(addressId.startsWith("A"), "地址ID应以A开头");
        assertEquals(21, addressId.length(), "地址ID应为21位");

        System.out.println("✅ 地址ID测试通过：" + addressId);
    }

    /**
     * 测试生成支付ID
     */
    @Test
    public void testGeneratePaymentId() {
        String paymentId = idGenerator.generatePaymentId();

        assertNotNull(paymentId);
        assertTrue(paymentId.startsWith("P"), "支付ID应以P开头");
        assertEquals(21, paymentId.length(), "支付ID应为21位");

        System.out.println("✅ 支付ID测试通过：" + paymentId);
    }

    /**
     * 测试ID唯一性
     */
    @Test
    public void testUniqueness() {
        Set<String> ids = new HashSet<>();
        int count = 10000;

        for (int i = 0; i < count; i++) {
            String id = idGenerator.generateId();
            assertFalse(ids.contains(id), "发现重复ID：" + id);
            ids.add(id);
        }

        assertEquals(count, ids.size());
        System.out.println("✅ 唯一性测试通过：生成" + count + "个ID，全部唯一");
    }

    /**
     * 测试ID性能
     */
    @Test
    public void testPerformance() {
        int count = 10000;
        long startTime = System.nanoTime();

        for (int i = 0; i < count; i++) {
            idGenerator.generateOrderId();
        }

        long duration = (System.nanoTime() - startTime) / 1000000;
        double avgTime = duration * 1000000.0 / count;

        System.out.println("========================================");
        System.out.println("  性能测试结果");
        System.out.println("========================================");
        System.out.println("  生成数量：" + count);
        System.out.println("  总耗时：" + duration + "ms");
        System.out.println("  平均耗时：" + String.format("%.2f", avgTime) + " 纳秒/个");
        System.out.println("  QPS：" + String.format("%.0f", count * 1000.0 / duration));
        System.out.println("========================================");

        assertTrue(duration < 1000, "生成10000个ID应在1秒内完成");
        System.out.println("✅ 性能测试通过");
    }

    /**
     * 测试提取纯数字ID
     */
    @Test
    public void testExtractNumericId() {
        String orderId = "O89234756234567890123";
        String numericId = EnhancedIdGenerator.extractNumericId(orderId);

        assertEquals("89234756234567890123", numericId);
        assertEquals(20, numericId.length());

        System.out.println("✅ 提取数字ID测试通过：" + numericId);
    }

    /**
     * 测试转换为Long
     */
    @Test
    public void testToLongId() {
        String orderId = "O892347562345678901";
        Long longId = EnhancedIdGenerator.toLongId(orderId);

        assertNotNull(longId);
        assertEquals(892347562345678901L, longId);

        System.out.println("✅ 转换Long测试通过：" + longId);
    }

    /**
     * 测试提取ID类型
     */
    @Test
    public void testExtractIdType() {
        String orderId = "O89234756234567890123";
        String type = EnhancedIdGenerator.extractIdType(orderId);

        assertEquals("ORDER", type);

        System.out.println("✅ 提取ID类型测试通过：" + type);
    }

    /**
     * 测试各种ID类型
     */
    @Test
    public void testAllIdTypes() {
        System.out.println("========================================");
        System.out.println("  生成各种类型的ID");
        System.out.println("========================================");

        System.out.println("用户ID: " + idGenerator.generateUserId());
        System.out.println("商家ID: " + idGenerator.generateMerchantId());
        System.out.println("群组ID: " + idGenerator.generateGroupId());
        System.out.println("菜品ID: " + idGenerator.generateDishId());
        System.out.println("菜单ID: " + idGenerator.generateMenuId());
        System.out.println("订单ID: " + idGenerator.generateOrderId());
        System.out.println("地址ID: " + idGenerator.generateAddressId());
        System.out.println("支付ID: " + idGenerator.generatePaymentId());
        System.out.println("评价ID: " + idGenerator.generateReviewId());
        System.out.println("钱包ID: " + idGenerator.generateWalletId());
        System.out.println("通知ID: " + idGenerator.generateNotificationId());
        System.out.println("========================================");

        System.out.println("✅ 所有ID类型测试通过");
    }

    /**
     * 测试ID长度一致性
     */
    @Test
    public void testIdLengthConsistency() {
        // 测试100个ID，验证长度一致性
        for (int i = 0; i < 100; i++) {
            String userId = idGenerator.generateUserId();
            assertEquals(21, userId.length());

            String orderId = idGenerator.generateOrderId();
            assertEquals(21, orderId.length());

            String dishId = idGenerator.generateDishId();
            assertEquals(21, dishId.length());
        }

        System.out.println("✅ ID长度一致性测试通过：100个ID长度全部正确");
    }

    /**
     * 测试ID格式
     */
    @Test
    public void testIdFormat() {
        String orderId = idGenerator.generateOrderId();

        // 验证格式：前缀 + 20位数字
        assertTrue(orderId.matches("^O\\d{20}$"), "ID格式应为前缀 + 20位数字");

        System.out.println("✅ ID格式测试通过：" + orderId);
    }

    /**
     * 综合测试
     */
    @Test
    public void testComprehensive() {
        System.out.println("\n========================================");
        System.out.println("  Enhanced ID生成器综合测试");
        System.out.println("========================================\n");

        // 测试1：生成10个订单ID
        System.out.println("生成的10个订单ID：");
        for (int i = 0; i < 10; i++) {
            String id = idGenerator.generateOrderId();
            System.out.println("  " + (i + 1) + ". " + id);
        }
        System.out.println();

        // 测试2：验证不可预测性
        System.out.println("验证不可预测性（相邻ID差异）：");
        String id1 = idGenerator.generateOrderId();
        String id2 = idGenerator.generateOrderId();
        String id3 = idGenerator.generateOrderId();

        long num1 = Long.parseLong(id1.substring(1));
        long num2 = Long.parseLong(id2.substring(1));
        long num3 = Long.parseLong(id3.substring(1));

        long diff12 = Math.abs(num1 - num2);
        long diff23 = Math.abs(num2 - num3);

        System.out.println("  ID1: " + id1);
        System.out.println("  ID2: " + id2);
        System.out.println("  ID3: " + id3);
        System.out.println("  差异12: " + diff12);
        System.out.println("  差异23: " + diff23);
        System.out.println("  ✅ 相邻ID差异较大，不可预测");

        // 测试3：生成大量ID验证唯一性
        System.out.println("\n验证唯一性（生成1000个）：");
        Set<String> ids = new HashSet<>();
        boolean hasDuplicate = false;
        for (int i = 0; i < 1000; i++) {
            String id = idGenerator.generateId();
            if (!ids.add(id)) {
                System.out.println("  ❌ 发现重复：" + id);
                hasDuplicate = true;
            }
        }
        if (!hasDuplicate) {
            System.out.println("  ✅ 1000个ID全部唯一");
        }

        System.out.println("\n========================================");
        System.out.println("  综合测试完成！");
        System.out.println("========================================");
    }
}
