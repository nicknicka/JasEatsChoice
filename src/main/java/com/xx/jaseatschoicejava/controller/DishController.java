package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品控制器
 */
@RestController
@RequestMapping("/dishes")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 获取菜品列表
     */
    @GetMapping
    public ResponseResult<?> getDishes(@RequestParam(required = false) String category,
                                      @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            queryWrapper.eq(Dish::getCategory, category);
        }
        if (keyword != null) {
            queryWrapper.like(Dish::getName, keyword);
        }
        // 只显示上架的菜品
        queryWrapper.eq(Dish::getStatus, true);
        List<Dish> dishes = dishService.list(queryWrapper);
        return ResponseResult.success(dishes);
    }

    /**
     * 获取菜品详情
     */
    @GetMapping("/{dishId}")
    public ResponseResult<?> getDishDetail(@PathVariable Long dishId) {
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
        boolean saved = dishService.save(dish);
        if (saved) {
            return ResponseResult.success("菜品创建成功");
        }
        return ResponseResult.fail("500", "菜品创建失败");
    }
}
