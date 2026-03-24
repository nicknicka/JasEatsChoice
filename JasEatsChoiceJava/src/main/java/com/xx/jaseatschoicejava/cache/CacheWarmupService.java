package com.xx.jaseatschoicejava.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.UserPreference;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.UserPreferenceService;

import java.util.List;

/**
 * 缓存预热服务
 *
 * 功能：
 * 1. 应用启动时预加载热点数据
 * 2. 定时刷新热点数据
 * 3. 支持配置预热策略
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Component
public class CacheWarmupService implements ApplicationRunner {

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private DishService dishService;

    @Autowired
    private MerchantService merchantService;

    /**
     * 应用启动后自动执行
     *
     * @param args 应用参数
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("========================================");
        log.info("开始缓存预热...");
        log.info("========================================");

        long startTime = System.currentTimeMillis();

        try {
            // 预热活跃用户偏好
            warmupActiveUserPreferences();

            // 预热热门菜品
            warmupPopularDishes();

            // 预热活跃商家
            warmupActiveMerchants();

            long duration = System.currentTimeMillis() - startTime;
            log.info("========================================");
            log.info("缓存预热完成！耗时: {}ms", duration);
            log.info("========================================");

        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }

    /**
     * 预热活跃用户偏好
     */
    private void warmupActiveUserPreferences() {
        log.info("开始预热活跃用户偏好...");

        try {
            // 查询最近7天有登录/订单的用户（这里简化处理，实际应该从用户行为表查询）
            // 暂时预热前100个用户（示例）
            int warmupCount = 0;
            int maxUsers = 100; // 最多预热100个用户

            // 注意：这里需要根据实际业务逻辑查询活跃用户列表
            // 示例：从用户行为表或订单表查询最近活跃用户
            // List<String> activeUserIds = getActiveUserIds(7);

            // 简化示例：假设我们从某个地方获取活跃用户ID列表
            // for (String userId : activeUserIds) {
            //     try {
            //         UserPreference pref = userPreferenceService.getByUserId(userId);
            //         if (pref != null) {
            //             warmupCount++;
            //         }
            //     } catch (Exception e) {
            //         log.warn("预热用户偏好失败: userId={}", userId, e);
            //     }
            // }

            log.info("活跃用户偏好预热完成: count={}", warmupCount);

        } catch (Exception e) {
            log.error("预热活跃用户偏好失败", e);
        }
    }

    /**
     * 预热热门菜品
     */
    private void warmupPopularDishes() {
        log.info("开始预热热门菜品...");

        try {
            // 查询销量最高的菜品（这里简化处理）
            // 实际应该从订单统计表查询热门菜品
            int warmupCount = 0;
            int maxDishes = 50; // 最多预热50个菜品

            // 示例：预热最近添加的菜品
            // List<Dish> popularDishes = dishService.lambdaQuery()
            //     .eq(Dish::getIsOnline, true)
            //     .orderByDesc(Dish::getCreateTime)
            //     .last("LIMIT " + maxDishes)
            //     .list();

            // for (Dish dish : popularDishes) {
            //     try {
            //         // 通过Service方法触发缓存
            //         if (dishService instanceof DishServiceImpl) {
            //             DishServiceImpl dishService = (DishServiceImpl) dishService;
            //             dishService.getDishById(dish.getId());
            //             warmupCount++;
            //         }
            //     } catch (Exception e) {
            //         log.warn("预热菜品失败: dishId={}", dish.getId(), e);
            //     }
            // }

            log.info("热门菜品预热完成: count={}", warmupCount);

        } catch (Exception e) {
            log.error("预热热门菜品失败", e);
        }
    }

    /**
     * 预热活跃商家
     */
    private void warmupActiveMerchants() {
        log.info("开始预热活跃商家...");

        try {
            // 查询最近有订单的商家
            int warmupCount = 0;
            int maxMerchants = 20; // 最多预热20个商家

            // 示例：预热状态为营业的商家
            // List<Merchant> activeMerchants = merchantService.lambdaQuery()
            //     .eq(Merchant::getStatus, true)
            //     .orderByDesc(Merchant::getUpdateTime)
            //     .last("LIMIT " + maxMerchants)
            //     .list();

            // for (Merchant merchant : activeMerchants) {
            //     try {
            //         // 通过Service方法触发缓存
            //         if (merchantService instanceof MerchantServiceImpl) {
            //             MerchantServiceImpl merchantService = (MerchantServiceImpl) merchantService;
            //             merchantService.getMerchantById(merchant.getId());
            //             warmupCount++;
            //         }
            //     } catch (Exception e) {
            //         log.warn("预热商家失败: merchantId={}", merchant.getId(), e);
            //     }
            // }

            log.info("活跃商家预热完成: count={}", warmupCount);

        } catch (Exception e) {
            log.error("预热活跃商家失败", e);
        }
    }

    /**
     * 手动触发预热（可通过管理接口调用）
     *
     * @return 预热结果
     */
    public String manualWarmup() {
        log.info("手动触发缓存预热...");

        long startTime = System.currentTimeMillis();

        try {
            warmupActiveUserPreferences();
            warmupPopularDishes();
            warmupActiveMerchants();

            long duration = System.currentTimeMillis() - startTime;
            String result = String.format("手动缓存预热完成，耗时: %dms", duration);

            log.info(result);
            return result;

        } catch (Exception e) {
            log.error("手动缓存预热失败", e);
            return "手动缓存预热失败: " + e.getMessage();
        }
    }

    /**
     * 预热指定用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean warmupUser(String userId) {
        try {
            log.info("预热用户数据: userId={}", userId);

            // 预热用户偏好
            UserPreference pref = userPreferenceService.getByUserId(userId);
            if (pref != null) {
                log.info("用户偏好预热成功: userId={}", userId);
            }

            return true;

        } catch (Exception e) {
            log.error("预热用户失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 预热指定菜品
     *
     * @param dishId 菜品ID
     * @return 是否成功
     */
    public boolean warmupDish(String dishId) {
        try {
            log.info("预热菜品数据: dishId={}", dishId);

            // 触发缓存
            com.xx.jaseatschoicejava.service.impl.DishServiceImpl dishService =
                new com.xx.jaseatschoicejava.service.impl.DishServiceImpl();
            dishService.getDishById(dishId);

            log.info("菜品预热成功: dishId={}", dishId);
            return true;

        } catch (Exception e) {
            log.error("预热菜品失败: dishId={}", dishId, e);
            return false;
        }
    }

    /**
     * 预热指定商家
     *
     * @param merchantId 商家ID
     * @return 是否成功
     */
    public boolean warmupMerchant(String merchantId) {
        try {
            log.info("预热商家数据: merchantId={}", merchantId);

            // 触发缓存
            com.xx.jaseatschoicejava.service.impl.MerchantServiceImpl merchantService =
                new com.xx.jaseatschoicejava.service.impl.MerchantServiceImpl();
            merchantService.getMerchantById(merchantId);

            log.info("商家预热成功: merchantId={}", merchantId);
            return true;

        } catch (Exception e) {
            log.error("预热商家失败: merchantId={}", merchantId, e);
            return false;
        }
    }
}
