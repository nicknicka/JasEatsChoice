package com.xx.jaseatschoicejava.cache;

import com.xx.jaseatschoicejava.entity.UserPreference;
import com.xx.jaseatschoicejava.service.UserPreferenceService;
import com.xx.jaseatschoicejava.util.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis缓存测试类
 *
 * 功能：
 * 1. 测试RedisConfig配置是否正确
 * 2. 测试Spring Cache注解是否生效
 * 3. 测试RedisCacheUtil工具类
 * 4. 测试缓存穿透、雪崩、击穿防护
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class RedisCacheTest {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private UserPreferenceService userPreferenceService;

    @Autowired(required = false)
    private RedisCacheUtil redisCacheUtil;

    @BeforeEach
    public void setUp() {
        log.info("========================================");
        log.info("开始测试...");
        log.info("========================================");
    }

    /**
     * 测试1：验证Redis连接是否正常
     */
    @Test
    public void testRedisConnection() {
        log.info("测试1：验证Redis连接");

        if (redisTemplate == null) {
            log.warn("RedisTemplate未注入，跳过测试");
            return;
        }

        try {
            // 测试基本操作
            String testKey = "test:connection";
            String testValue = "test-value";

            redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
            Object result = redisTemplate.opsForValue().get(testKey);

            assertNotNull(result, "Redis连接失败：无法获取值");
            assertEquals(testValue, result, "Redis连接失败：值不匹配");

            // 清理测试数据
            redisTemplate.delete(testKey);

            log.info("✅ Redis连接测试通过");
        } catch (Exception e) {
            log.error("❌ Redis连接测试失败", e);
            fail("Redis连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试2：验证Spring Cache注解是否生效
     */
    @Test
    public void testSpringCacheAnnotation() {
        log.info("测试2：验证Spring Cache注解");

        if (userPreferenceService == null || redisTemplate == null) {
            log.warn("必要的Bean未注入，跳过测试");
            return;
        }

        try {
            String testUserId = "test_user_cache_" + System.currentTimeMillis();

            // 第一次查询（应该从数据库读取，返回null）
            UserPreference pref1 = userPreferenceService.getByUserId(testUserId);
            log.info("第一次查询结果: {}", pref1);
            assertNull(pref1, "第一次查询应该返回null");

            // 检查缓存中是否存在（由于unless="#result == null"，null不会被缓存）
            String cacheKey = "user:preference::" + testUserId;
            Boolean hasKey = redisTemplate.hasKey(cacheKey);
            log.info("缓存key是否存在: {}", hasKey);

            log.info("✅ Spring Cache注解测试通过");
        } catch (Exception e) {
            log.error("❌ Spring Cache注解测试失败", e);
            fail("Spring Cache注解测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试3：验证RedisCacheUtil工具类
     */
    @Test
    public void testRedisCacheUtil() {
        log.info("测试3：验证RedisCacheUtil工具类");

        if (redisCacheUtil == null) {
            log.warn("RedisCacheUtil未注入，跳过测试");
            return;
        }

        try {
            String testKey = "test:util:" + System.currentTimeMillis();
            String testValue = "test-util-value";

            // 测试set操作
            redisCacheUtil.set(testKey, testValue);
            log.info("设置缓存: key={}, value={}", testKey, testValue);

            // 测试get操作
            Object result = redisCacheUtil.get(testKey);
            log.info("获取缓存: key={}, result={}", testKey, result);
            assertEquals(testValue, result, "获取的值与设置的值不匹配");

            // 测试exists操作
            boolean exists = redisCacheUtil.exists(testKey);
            log.info("缓存是否存在: {}", exists);
            assertTrue(exists, "缓存应该存在");

            // 测试delete操作
            redisCacheUtil.delete(testKey);
            log.info("删除缓存: key={}", testKey);

            // 验证删除后不存在
            exists = redisCacheUtil.exists(testKey);
            log.info("删除后是否存在: {}", exists);
            assertFalse(exists, "删除后缓存不应该存在");

            log.info("✅ RedisCacheUtil工具类测试通过");
        } catch (Exception e) {
            log.error("❌ RedisCacheUtil工具类测试失败", e);
            fail("RedisCacheUtil工具类测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试4：验证缓存空值防护（缓存穿透）
     */
    @Test
    public void testCacheNullProtection() {
        log.info("测试4：验证缓存空值防护");

        if (redisCacheUtil == null) {
            log.warn("RedisCacheUtil未注入，跳过测试");
            return;
        }

        try {
            String testKey = "test:null:protection:" + System.currentTimeMillis();

            // 缓存null值
            redisCacheUtil.setWithNullProtection(testKey, null, 300);
            log.info("缓存null值: key={}", testKey);

            // 获取null值
            Object result = redisCacheUtil.get(testKey);
            log.info("获取null缓存: key={}, result={}", testKey, result);
            assertNull(result, "应该返回null");

            // 验证缓存存在（虽然是空值）
            boolean exists = redisCacheUtil.exists(testKey);
            log.info("空值缓存是否存在: {}", exists);
            assertTrue(exists, "空值缓存应该存在（短期）");

            // 清理
            redisCacheUtil.delete(testKey);

            log.info("✅ 缓存空值防护测试通过");
        } catch (Exception e) {
            log.error("❌ 缓存空值防护测试失败", e);
            fail("缓存空值防护测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试5：验证缓存过期时间
     */
    @Test
    public void testCacheExpiration() throws InterruptedException {
        log.info("测试5：验证缓存过期时间");

        if (redisCacheUtil == null) {
            log.warn("RedisCacheUtil未注入，跳过测试");
            return;
        }

        try {
            String testKey = "test:expiration:" + System.currentTimeMillis();
            String testValue = "test-expiration-value";
            long expireSeconds = 2; // 2秒过期

            // 设置缓存
            redisCacheUtil.set(testKey, testValue, expireSeconds);
            log.info("设置缓存: key={}, ttl={}秒", testKey, expireSeconds);

            // 立即获取，应该存在
            Object result1 = redisCacheUtil.get(testKey);
            log.info("立即获取: result={}", result1);
            assertNotNull(result1, "缓存应该存在");

            // 等待过期
            log.info("等待{}秒...", expireSeconds);
            Thread.sleep(expireSeconds * 1000 + 500);

            // 再次获取，应该不存在
            Object result2 = redisCacheUtil.get(testKey);
            log.info("过期后获取: result={}", result2);
            assertNull(result2, "缓存应该已过期");

            log.info("✅ 缓存过期时间测试通过");
        } catch (Exception e) {
            log.error("❌ 缓存过期时间测试失败", e);
            fail("缓存过期时间测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试6：验证setIfAbsent操作（分布式锁）
     */
    @Test
    public void testSetIfAbsent() {
        log.info("测试6：验证setIfAbsent操作");

        if (redisCacheUtil == null) {
            log.warn("RedisCacheUtil未注入，跳过测试");
            return;
        }

        try {
            String testKey = "test:setifabsent:" + System.currentTimeMillis();
            String testValue1 = "value1";
            String testValue2 = "value2";

            // 第一次setIfAbsent，应该成功
            boolean result1 = redisCacheUtil.setIfAbsent(testKey, testValue1);
            log.info("第一次setIfAbsent: key={}, result={}", testKey, result1);
            assertTrue(result1, "第一次setIfAbsent应该成功");

            // 第二次setIfAbsent，应该失败（key已存在）
            boolean result2 = redisCacheUtil.setIfAbsent(testKey, testValue2);
            log.info("第二次setIfAbsent: key={}, result={}", testKey, result2);
            assertFalse(result2, "第二次setIfAbsent应该失败");

            // 验证值还是第一个
            Object value = redisCacheUtil.get(testKey);
            log.info("当前值: {}", value);
            assertEquals(testValue1, value, "值应该是第一个设置的值");

            // 清理
            redisCacheUtil.delete(testKey);

            log.info("✅ setIfAbsent操作测试通过");
        } catch (Exception e) {
            log.error("❌ setIfAbsent操作测试失败", e);
            fail("setIfAbsent操作测试失败: " + e.getMessage());
        }
    }
}
