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
        queryWrapper.eq(Dish::getStatus, true); // 只返回上架的菜品
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
     * 获取菜品详情
     */
    @GetMapping("/{dishId}")
    public ResponseResult<?> getDishDetail(@PathVariable String dishId) {
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new BusinessException("404", "菜品不存在");
        }
        if (!dish.getStatus()) {
            throw new BusinessException("400", "菜品已下架");
        }
        return ResponseResult.success(dish);
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
}
