package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.MenuWithDishStatusDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品控制器
 */
@Slf4j
@RestController
@RequestMapping("/dishes")
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
        log.info("获取菜品列表 {} ", merchantId);
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
        return ResponseResult.success(dishes);
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
