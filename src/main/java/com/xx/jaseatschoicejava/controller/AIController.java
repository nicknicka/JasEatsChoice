package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI能力控制器
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    /**
     * AI菜品识别
     */
    @PostMapping("/dish-recognize")
    public ResponseResult<?> dishRecognize(@RequestBody Map<String, Object> params) {
        // TODO: Integrate with third-party AI service for dish recognition
        // For now, return mock data

        List<Map<String, Object>> dishes = new ArrayList<>();

        Map<String, Object> dish1 = new HashMap<>();
        dish1.put("name", "宫保鸡丁");
        dish1.put("calorie", 150.5);
        dish1.put("difficulty", "easy");
        dish1.put("ingredients", "鸡肉, 花生米, 辣椒");
        dish1.put("steps", "1. 鸡肉切丁; 2. 炒制; 3. 加入花生米");

        Map<String, Object> dish2 = new HashMap<>();
        dish2.put("name", "番茄炒蛋");
        dish2.put("calorie", 120.3);
        dish2.put("difficulty", "easy");
        dish2.put("ingredients", "番茄, 鸡蛋");
        dish2.put("steps", "1. 鸡蛋打散; 2. 炒鸡蛋; 3. 加入番茄");

        dishes.add(dish1);
        dishes.add(dish2);

        return ResponseResult.success(dishes);
    }

    /**
     * AI食谱上传与优化
     */
    @PostMapping("/recipe-upload")
    public ResponseResult<?> recipeUpload(@RequestBody Map<String, Object> params) {
        // TODO: Integrate with third-party AI service for recipe optimization
        // For now, return mock data with optimized recipe

        Map<String, Object> recipe = (Map<String, Object>) params.get("recipe");
        Map<String, Object> optimizedRecipe = new HashMap<>(recipe);

        // Add mock optimization
        optimizedRecipe.put("optimization", "AI优化建议：减少油盐用量，增加蔬菜比例");
        optimizedRecipe.put("calorie", 250.0); // Mock calorie calculation

        return ResponseResult.success(optimizedRecipe);
    }
}
