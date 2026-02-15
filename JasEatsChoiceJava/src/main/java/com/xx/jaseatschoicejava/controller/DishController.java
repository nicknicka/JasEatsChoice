package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.MenuWithDishStatusDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜品控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/dishes")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private MenuService menuService;

    /**
     * 获取菜品列表
     */
    @GetMapping
    public ResponseResult<?> getDishes(@RequestParam(required = false) String category,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String merchantId) {
        log.info("获取菜品列表, merchantId: {}", merchantId);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            queryWrapper.eq(Dish::getCategory, category);
        }
        if (keyword != null) {
            queryWrapper.like(Dish::getName, keyword);
        }
        if (merchantId != null) {
            queryWrapper.eq(Dish::getMerchantId, merchantId);
        }
        List<Dish> dishes = dishService.list(queryWrapper);

        // 转换为包含食材数据的Map列表
        List<Map<String, Object>> resultDishes = dishes.stream().map(dish -> {
            Map<String, Object> dishMap = new HashMap<>();
            dishMap.put("id", dish.getId());
            dishMap.put("name", dish.getName());
            dishMap.put("price", dish.getPrice());
            dishMap.put("category", dish.getCategory());
            dishMap.put("description", dish.getDescription());
            dishMap.put("calorie", dish.getCalorie());
            dishMap.put("image", dish.getImage());
            dishMap.put("status", dish.getStatus());
            dishMap.put("merchantId", dish.getMerchantId());

            // 解析食材数据
            Map<String, Object> ingredientsData = parseIngredients(dish.getIngredients());
            dishMap.put("optionalIngredients", ingredientsData.get("optionalIngredients"));
            dishMap.put("requiredIngredients", ingredientsData.get("requiredIngredients"));

            return dishMap;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseResult.success(resultDishes);
    }

    /**
     * 根据商家ID获取菜品列表
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseResult<?> getDishesByMerchant(@PathVariable String merchantId) {
        log.info("根据商家ID获取菜品列表, merchantId: {}", merchantId);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getMerchantId, merchantId);
        queryWrapper.eq(Dish::getIsOnline, true); // 只返回上架的菜品（使用isOnline而不是status）
        List<Dish> dishes = dishService.list(queryWrapper);

        // 转换为包含食材数据的Map列表
        List<Map<String, Object>> resultDishes = dishes.stream().map(dish -> {
            Map<String, Object> dishMap = new HashMap<>();
            dishMap.put("id", dish.getId());
            dishMap.put("name", dish.getName());
            dishMap.put("price", dish.getPrice());
            dishMap.put("category", dish.getCategory());
            dishMap.put("description", dish.getDescription());
            dishMap.put("calorie", dish.getCalorie());
            dishMap.put("image", dish.getImage());
            dishMap.put("status", dish.getStatus());
            dishMap.put("merchantId", dish.getMerchantId());

            // 解析食材数据
            Map<String, Object> ingredientsData = parseIngredients(dish.getIngredients());
            dishMap.put("optionalIngredients", ingredientsData.get("optionalIngredients"));
            dishMap.put("requiredIngredients", ingredientsData.get("requiredIngredients"));

            return dishMap;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseResult.success(resultDishes);
    }

    /**
     * 解析食材JSON字符串
     * 支持多种JSON格式，返回前端需要的格式
     */
    private Map<String, Object> parseIngredients(String ingredientsJson) {
        Map<String, Object> result = new HashMap<>();
        List<Object> optionalIngredients = new ArrayList<>();
        List<String> requiredIngredients = new ArrayList<>();

        // 如果食材为空或null，返回空数组
        if (ingredientsJson == null || ingredientsJson.trim().isEmpty()) {
            result.put("optionalIngredients", optionalIngredients);
            result.put("requiredIngredients", requiredIngredients);
            return result;
        }

        try {
            // 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();
            // 尝试解析为Map
            @SuppressWarnings("unchecked")
            Map<String, Object> ingredientsMap = objectMapper.readValue(ingredientsJson, Map.class);

            // 处理必选食材（支持多种字段名）
            Object required = ingredientsMap.get("requiredIngredients");
            if (required == null) {
                required = ingredientsMap.get("required");
            }
            if (required == null) {
                required = ingredientsMap.get("mandatory"); // 前端使用的字段名
            }
            if (required instanceof List) {
                for (Object item : (List<?>) required) {
                    if (item != null) {
                        requiredIngredients.add(item.toString());
                    }
                }
            }

            // 处理可选食材
            Object optional = ingredientsMap.get("optionalIngredients");
            if (optional == null) {
                optional = ingredientsMap.get("optional");
            }
            if (optional instanceof List) {
                for (Object item : (List<?>) optional) {
                    if (item != null) {
                        // 如果是 Map，转换为可选食材对象
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> ingredientMap = (Map<String, Object>) item;
                            Map<String, Object> ingredient = new HashMap<>();
                            ingredient.put("id", ingredientMap.get("id"));
                            ingredient.put("name", ingredientMap.get("name"));
                            ingredient.put("price", ingredientMap.get("price"));
                            ingredient.put("selected", false);
                            optionalIngredients.add(ingredient);
                        } else {
                            optionalIngredients.add(item);
                        }
                    }
                }
            }

        } catch (Exception e) {
            // JSON解析失败，返回空数组
            log.error("解析食材JSON失败: {}", ingredientsJson, e);
        }

        result.put("optionalIngredients", optionalIngredients);
        result.put("requiredIngredients", requiredIngredients);
        return result;
    }

    /**
     * 获取菜品详情（包含详细信息、分类、推荐等）
     */
    @GetMapping("/{dishId}")
    public ResponseResult<?> getDishDetail(@PathVariable String dishId) {
        log.info("获取菜品详情 - dishId: {}", dishId);

        // 获取菜品基本信息
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new BusinessException("404", "菜品不存在");
        }
        if (!dish.getStatus()) {
            throw new BusinessException("400", "菜品已下架");
        }

        // 构建返回数据Map
        Map<String, Object> dishData = new HashMap<>();
        dishData.put("id", dish.getId());
        dishData.put("dishId", dish.getId());
        dishData.put("name", dish.getName());
        dishData.put("price", dish.getPrice());
        dishData.put("category", dish.getCategory());
        dishData.put("description", dish.getDescription());
        dishData.put("image", dish.getImage());
        dishData.put("status", dish.getStatus());
        dishData.put("merchantId", dish.getMerchantId());
        dishData.put("merchantName", ""); // 需要查询商家表填充

        // 处理食材数据
        Map<String, Object> ingredientsData = parseIngredients(dish.getIngredients());
        dishData.put("requiredIngredients", ingredientsData.get("requiredIngredients"));
        dishData.put("optionalIngredients", ingredientsData.get("optionalIngredients"));
        dishData.put("hasAllergens", detectAllergens(dish.getIngredients()));

        // 处理烹饪步骤
        List<Map<String, Object>> cookingSteps = parseCookingSteps(dish.getCookingSteps());
        dishData.put("cookingSteps", cookingSteps);

        // 处理营养信息
        Map<String, Object> nutritionData = parseNutrition(dish.getNutrition());
        dishData.put("nutrition", nutritionData);

        // 检查购物车状态
        dishData.put("inCart", false); // 默认false，需要查询

        // 获取相关菜品推荐
        List<Map<String, Object>> relatedDishes = getRelatedDishes(dish.getId(), dish.getCategory(), 5);
        dishData.put("relatedDishes", relatedDishes);

        log.info("返回菜品详情成功 - dishId: {}, 包含字段: {}", dishId, dishData.keySet());
        return ResponseResult.success(dishData);
    }

    /**
     * 创建菜品
     */
    @PostMapping
    public ResponseResult<?> createDish(@RequestBody Dish dish) {
        // 设置默认值
        if (dish.getStatus() == null) {
            dish.setStatus(true); // 默认上架
        }
        boolean saved = dishService.save(dish);
        if (saved) {
            return ResponseResult.success(dish); // 返回创建的菜品数据
        }
        return ResponseResult.fail("500", "菜品创建失败");
    }

    /**
     * 更新菜品
     */
    @PutMapping("/{dishId}")
    public ResponseResult<?> updateDish(@PathVariable String dishId, @RequestBody Dish dish) {
        dish.setId(dishId);
        boolean updated = dishService.updateById(dish);
        if (updated) {
            return ResponseResult.success(dishService.getById(dishId)); // 返回更新后的菜品数据
        }
        return ResponseResult.fail("500", "菜品更新失败");
    }

    /**
     * 更新菜品状态（上架/下架）
     */
    @PutMapping("/{dishId}/status")
    public ResponseResult<?> updateDishStatus(@PathVariable String dishId, @RequestBody java.util.Map<String, Object> request) {
        Boolean status = (Boolean) request.get("status");
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new BusinessException("404", "菜品不存在");
        }
        dish.setStatus(status);
        boolean updated = dishService.updateById(dish);
            log.info("更新菜品状态 {} {}", dishId, status);
        log.info("updated {} ", updated);
        if (updated) {
            // 当菜品下架时，同步更新该菜品在所有菜单中的状态为下架
            if (!status) {
                // 获取该菜品关联的所有菜单
                List<MenuWithDishStatusDTO> menus = menuService.getMenusByDishId(String.valueOf(dishId));
                if (menus != null && !menus.isEmpty()) {
                    List<String> menuIds = menus.stream().map(menu -> menu.getId()).collect(java.util.stream.Collectors.toList());
                    menuService.batchUpdateDishStatusInMenus(String.valueOf(dishId), menuIds, 0); // 0 表示下架
                }
            }

            return ResponseResult.success("菜品状态更新成功");
        }
        return ResponseResult.fail("500", "菜品状态更新失败");
    }

    /**
     * 批量删除菜品
     */
    @PutMapping("/batch")
    public ResponseResult<?> batchDeleteDishes(@RequestBody java.util.Map<String, Object> request) {
        List<Long> dishIds = (List<Long>) request.get("dishIds");

        if (dishIds == null || dishIds.isEmpty()) {
            return ResponseResult.fail("400", "请选择要删除的菜品");
        }

        log.info("批量删除菜品, dishIds: {}", dishIds);

        // 批量删除菜品
        boolean deleted = dishService.removeByIds(dishIds);

        if (deleted) {
            return ResponseResult.success("批量删除菜品成功");
        }
        return ResponseResult.fail("500", "批量删除菜品失败");
    }

    /**
     * 批量更新菜品状态（上架/下架）
     */
    @PutMapping("/batch/status")
    public ResponseResult<?> batchUpdateDishStatus(@RequestBody java.util.Map<String, Object> request) {
        List<Long> dishIds = (List<Long>) request.get("dishIds");
        Boolean status = (Boolean) request.get("status");

        if (dishIds == null || dishIds.isEmpty()) {
            return ResponseResult.fail("400", "请选择要操作的菜品");
        }

        List<Dish> dishes = dishService.listByIds(dishIds);
        for (Dish dish : dishes) {
            dish.setStatus(status);
        }
        boolean updated = dishService.updateBatchById(dishes);

        if (updated) {
            return ResponseResult.success("批量更新菜品状态成功");
        }
        return ResponseResult.fail("500", "批量更新菜品状态失败");
    }

    /**
     * 检测食材中的过敏原
     */
    private boolean detectAllergens(String ingredientsJson) {
        if (ingredientsJson == null || ingredientsJson.isEmpty()) {
            return false;
        }
        // 简化实现：检查是否包含常见过敏原关键词
        String[] commonAllergens = {"花生", "坚果", "牛奶", "鸡蛋", "大豆", "小麦", "鱼", "贝类"};
        for (String allergen : commonAllergens) {
            if (ingredientsJson.contains(allergen)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析烹饪步骤
     */
    private List<Map<String, Object>> parseCookingSteps(String cookingStepsJson) {
        List<Map<String, Object>> steps = new ArrayList<>();
        if (cookingStepsJson == null || cookingStepsJson.isEmpty()) {
            return steps;
        }
        try {
            // 简化实现：返回空列表，实际应该解析JSON
            return steps;
        } catch (Exception e) {
            log.error("解析烹饪步骤失败", e);
            return steps;
        }
    }

    /**
     * 解析营养信息
     */
    private Map<String, Object> parseNutrition(String nutritionJson) {
        Map<String, Object> nutrition = new HashMap<>();
        if (nutritionJson == null || nutritionJson.isEmpty()) {
            return nutrition;
        }
        try {
            // 简化实现：返回空Map，实际应该解析JSON
            return nutrition;
        } catch (Exception e) {
            log.error("解析营养信息失败", e);
            return nutrition;
        }
    }

    /**
     * 获取相关菜品推荐
     */
    private List<Map<String, Object>> getRelatedDishes(String dishId, String category, int limit) {
        List<Map<String, Object>> relatedDishes = new ArrayList<>();
        try {
            // 查找同分类的其他菜品
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dish::getIsOnline, true);
            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }
            queryWrapper.ne(Dish::getId, dishId);
            queryWrapper.last("LIMIT " + limit);

            List<Dish> dishes = dishService.list(queryWrapper);
            for (Dish d : dishes) {
                Map<String, Object> dishInfo = new HashMap<>();
                dishInfo.put("id", d.getId());
                dishInfo.put("dishId", d.getId());
                dishInfo.put("name", d.getName());
                dishInfo.put("image", d.getImage());
                dishInfo.put("price", d.getPrice());
                relatedDishes.add(dishInfo);
            }
        } catch (Exception e) {
            log.error("获取相关菜品失败", e);
        }
        return relatedDishes;
    }

    /**
     * 获取可替换的菜品推荐
     * @param type 餐型（breakfast/lunch/dinner等）
     * @param exclude 排除的菜品名称
     * @param limit 返回数量限制
     * @return 推荐菜品列表
     */
    @GetMapping("/replacement")
    public ResponseResult<?> getReplacementDishes(@RequestParam String type,
                                                  @RequestParam(required = false) String exclude,
                                                  @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取可替换菜品推荐, type: {}, exclude: {}, limit: {}", type, exclude, limit);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategory, type);
        queryWrapper.eq(Dish::getIsOnline, true);

        // 排除当前菜品
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(exclude)) {
            queryWrapper.ne(Dish::getName, exclude);
        }

        // 按推荐得分和评分排序
        queryWrapper.orderByDesc(Dish::getScore)
                  .orderByDesc(Dish::getAvgRating)
                  .last("LIMIT " + limit);

        List<Dish> dishes = dishService.list(queryWrapper);

        // 转换为前端需要的格式
        List<Map<String, Object>> resultDishes = new ArrayList<>();
        for (Dish dish : dishes) {
            Map<String, Object> dishMap = new HashMap<>();
            dishMap.put("id", dish.getId());
            dishMap.put("name", dish.getName());
            dishMap.put("type", dish.getCategory());
            dishMap.put("calorie", dish.getCalorie());
            dishMap.put("calories", dish.getCalorie()); // 兼容字段
            dishMap.put("price", dish.getPrice());
            dishMap.put("image", dish.getImage());
            dishMap.put("description", dish.getDescription());
            dishMap.put("merchantId", dish.getMerchantId());

            // 解析营养信息
            Map<String, Object> nutritionData = parseNutrition(dish.getNutrition());
            dishMap.put("nutrition", nutritionData);

            // 解析食材数据
            Map<String, Object> ingredientsData = parseIngredients(dish.getIngredients());
            dishMap.put("ingredients", ingredientsData.get("requiredIngredients"));

            // 如果营养数据中有详细值，使用实际值，否则使用卡路里
            if (nutritionData != null && !nutritionData.isEmpty()) {
                dishMap.put("protein", nutritionData.get("protein") != null ? nutritionData.get("protein") : 0);
                dishMap.put("carbs", nutritionData.get("carbs") != null ? nutritionData.get("carbs") : 0);
                dishMap.put("fat", nutritionData.get("fat") != null ? nutritionData.get("fat") : 0);
            } else {
                dishMap.put("protein", 0);
                dishMap.put("carbs", 0);
                dishMap.put("fat", 0);
            }

            resultDishes.add(dishMap);
        }

        log.info("返回可替换菜品列表, 数量: {}", resultDishes.size());
        return ResponseResult.success(resultDishes);
    }
}
