package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 经营品类控制器
 */
@RestController
@RequestMapping("/v1/category")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    /**
     * 常用品类列表（快捷选择）
     */
    private static final List<String> COMMON_CATEGORIES = Arrays.asList(
        "中式快餐", "火锅", "烧烤", "川菜", "湘菜", "粤菜", "西餐", "日韩料理"
    );

    /**
     * 所有品类列表（用于搜索）
     */
    private static final List<String> ALL_CATEGORIES = Arrays.asList(
        "中式快餐", "火锅", "烧烤", "川菜", "湘菜", "粤菜", "西餐", "日韩料理",
        "东北菜", "西北菜", "云南菜", "贵州菜", "鲁菜", "苏菜", "浙菜", "闽菜",
        "徽菜", "楚菜", "本帮菜", "客家菜", "潮汕菜", "新疆菜", "藏菜", "清真菜",
        "东南亚菜", "韩国料理", "日本料理", "意大利菜", "法国菜", "美式快餐",
        "披萨", "汉堡", "三明治", "沙拉", "寿司", "刺身", "拉面", "炸鸡",
        "烧烤", "麻辣烫", "冒菜", "串串香", "铁板烧", "涮涮锅", "麻辣香锅",
        "酸菜鱼", "烤鱼", "小龙虾", "大排档", "小吃快餐", "饮品甜点", "咖啡",
        "奶茶", "果汁", "冰淇淋", "蛋糕", "面包甜点", "茶餐厅", "早餐", "夜宵"
    );

    /**
     * 获取常用品类接口
     * 用于快捷选择功能，返回最常用的8个品类
     *
     * @return 常用品类列表
     */
    @GetMapping("/common")
    public ResponseResult<List<String>> getCommonCategories() {
        try {
            logger.info("获取常用品类列表");
            return ResponseResult.success(COMMON_CATEGORIES, "获取常用品类成功");
        } catch (Exception e) {
            logger.error("获取常用品类失败", e);
            return ResponseResult.fail("500", "获取常用品类失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有品类接口
     * 用于搜索功能，返回所有可用的品类
     *
     * @return 所有品类列表
     */
    @GetMapping("/list")
    public ResponseResult<List<String>> getAllCategories() {
        try {
            logger.info("获取所有品类列表");
            return ResponseResult.success(ALL_CATEGORIES, "获取所有品类成功");
        } catch (Exception e) {
            logger.error("获取所有品类失败", e);
            return ResponseResult.fail("500", "获取所有品类失败: " + e.getMessage());
        }
    }
}
