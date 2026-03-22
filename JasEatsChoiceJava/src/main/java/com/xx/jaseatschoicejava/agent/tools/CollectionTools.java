package com.xx.jaseatschoicejava.agent.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.UserCollection;
import com.xx.jaseatschoicejava.service.CollectionService;
import com.xx.jaseatschoicejava.service.DishService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 收藏管理工具集
 * 使用LangChain4j的@Tool注解声明工具函数
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class CollectionTools {

    private static final Logger log = LoggerFactory.getLogger(CollectionTools.class);

    @Resource
    private CollectionService collectionService;

    @Resource
    private DishService dishService;

    /**
     * 添加菜品到收藏夹
     *
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @return 操作结果
     */
    @Tool("添加菜品到收藏夹，支持收藏喜欢的菜品")
    public String addFavorite(String userId, String dishId) {
        log.info("执行工具：addFavorite，用户：{}，菜品：{}", userId, dishId);

        try {
            // 检查菜品是否存在
            Dish dish = dishService.getById(dishId);
            if (dish == null) {
                return "❌ 未找到该菜品，请确认菜品ID是否正确。";
            }

            // 检查是否已收藏
            boolean isFavorited = collectionService.isCollected(userId, "dish", dishId);
            if (isFavorited) {
                return String.format("✅ 「%s」已经在您的收藏夹中了，无需重复收藏。", dish.getName());
            }

            // 添加收藏
            UserCollection collection = new UserCollection();
            collection.setUserId(userId);
            collection.setCollectableType("dish");
            collection.setCollectableId(dishId);

            UserCollection result = collectionService.addCollection(collection);

            if (result != null) {
                return String.format("✅ 已成功将「%s」添加到收藏夹！\n\n💡 提示：您可以说「我的收藏」查看所有收藏的菜品。", dish.getName());
            } else {
                return "❌ 添加收藏失败，请稍后重试。";
            }

        } catch (Exception e) {
            log.error("添加收藏失败", e);
            return "添加收藏失败：" + e.getMessage();
        }
    }

    /**
     * 获取用户的收藏列表
     *
     * @param userId 用户ID
     * @return 收藏列表
     */
    @Tool("获取用户的收藏列表，包括菜名、价格、热量、评分等")
    public String getFavorites(String userId) {
        log.info("执行工具：getFavorites，用户：{}", userId);

        try {
            List<UserCollection> collections = collectionService.getCollectionsByUserId(userId);

            if (collections == null || collections.isEmpty()) {
                return "📚 **您的收藏列表**\n\n" +
                       "您还没有收藏任何菜品哦~\n\n" +
                       "💡 遇到喜欢的菜时，可以说：「把宫保鸡丁加入收藏」";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("📚 **您的收藏列表**（共%d个）\n\n", collections.size()));

            // 获取菜品详情
            for (int i = 0; i < collections.size(); i++) {
                UserCollection collection = collections.get(i);
                if ("dish".equals(collection.getCollectableType())) {
                    Dish dish = dishService.getById(collection.getCollectableId());
                    if (dish != null) {
                        result.append(String.format("**%d. %s**\n", i + 1, dish.getName()));
                        result.append(String.format("   💰 价格：¥%.2f\n", dish.getPrice()));

                        if (dish.getCalorie() != null && dish.getCalorie() > 0) {
                            result.append(String.format("   🔥 热量：%d kcal\n", dish.getCalorie()));
                        }

                        if (dish.getCategory() != null) {
                            result.append(String.format("   🍽️ 分类：%s\n", dish.getCategory()));
                        }

                        result.append("\n");
                    }
                }
            }

            result.append("💡 提示：\n");
            result.append("- 可以说「我要收藏中的第X个」来下单\n");
            result.append("- 可以说「取消收藏XX」来移除收藏");

            return result.toString();

        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return "获取收藏列表失败：" + e.getMessage();
        }
    }

    /**
     * 从收藏夹移除菜品
     *
     * @param userId 用户ID
     * @param dishId 菜品ID或菜品名称
     * @return 操作结果
     */
    @Tool("从收藏夹移除菜品，支持按菜品ID或菜品名称删除")
    public String removeFavorite(String userId, String dishId) {
        log.info("执行工具：removeFavorite，用户：{}，菜品：{}", userId, dishId);

        try {
            // 先尝试作为ID查找
            Dish dish = dishService.getById(dishId);

            // 如果不是ID，尝试作为名称查找
            if (dish == null) {
                List<UserCollection> collections = collectionService.getCollectionsByUserId(userId);
                for (UserCollection collection : collections) {
                    if ("dish".equals(collection.getCollectableType())) {
                        Dish favDish = dishService.getById(collection.getCollectableId());
                        if (favDish != null && (favDish.getName().contains(dishId) || dishId.contains(favDish.getName()))) {
                            dish = favDish;
                            break;
                        }
                    }
                }
            }

            if (dish == null) {
                return "❌ 未找到该菜品，请确认菜品名称或ID是否正确。\n\n💡 您可以说「我的收藏」查看所有收藏的菜品。";
            }

            // 检查是否已收藏
            boolean isFavorited = collectionService.isCollected(userId, "dish", dish.getId());
            if (!isFavorited) {
                return String.format("⚠️ 「%s」不在您的收藏夹中，无需移除。", dish.getName());
            }

            // 移除收藏
            boolean success = collectionService.removeCollection(userId, "dish", dish.getId());

            if (success) {
                return String.format("✅ 已成功将「%s」从收藏夹移除。\n\n💡 您可以说「我的收藏」查看剩余收藏。", dish.getName());
            } else {
                return "❌ 移除收藏失败，请稍后重试。";
            }

        } catch (Exception e) {
            log.error("移除收藏失败", e);
            return "移除收藏失败：" + e.getMessage();
        }
    }

    /**
     * 检查菜品是否已收藏
     *
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @return 是否已收藏
     */
    @Tool("检查菜品是否已在收藏夹中")
    public String isFavorited(String userId, String dishId) {
        log.info("执行工具：isFavorited，用户：{}，菜品：{}", userId, dishId);

        try {
            Dish dish = dishService.getById(dishId);
            if (dish == null) {
                return "❌ 未找到该菜品";
            }

            boolean isFavorited = collectionService.isCollected(userId, "dish", dishId);

            if (isFavorited) {
                return String.format("✅ 「%s」已在您的收藏夹中", dish.getName());
            } else {
                return String.format("⭕ 「%s」尚未收藏", dish.getName());
            }

        } catch (Exception e) {
            log.error("检查收藏状态失败", e);
            return "检查收藏状态失败：" + e.getMessage();
        }
    }
}
