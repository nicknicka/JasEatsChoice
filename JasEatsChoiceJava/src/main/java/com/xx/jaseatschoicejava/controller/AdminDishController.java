package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-菜品管理控制器
 */
@Api(tags = "管理员-菜品管理")
@RestController
@RequestMapping("/admin/dishes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取菜品列表（分页）
     */
    @ApiOperation("获取菜品列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:dish:list')")
    public ResponseEntity<IPage<Dish>> getDishList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        Page<Dish> pageParam = new Page<>(page, pageSize);
        IPage<Dish> result = dishService.page(pageParam);

        // 为每个菜品添加商家名称和状态代码
        result.getRecords().forEach(dish -> {
            // 设置商家名称
            if (dish.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(dish.getMerchantId());
                if (merchant != null) {
                    dish.setMerchantName(merchant.getName());
                }
            }

            // 设置状态代码
            dish.setStatusCode(dish.getStatus() ? "ACTIVE" : "INACTIVE");
        });

        return ResponseEntity.ok(result);
    }

    /**
     * 获取菜品详情
     */
    @ApiOperation("获取菜品详情")
    @GetMapping("/{dishId}")
    @PreAuthorize("hasAnyAuthority('admin:dish:list')")
    public ResponseEntity<Map<String, Object>> getDishDetail(@PathVariable String dishId) {
        Dish dish = dishService.getById(dishId);

        Map<String, Object> response = new HashMap<>();
        if (dish != null) {
            // 添加商家名称
            if (dish.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(dish.getMerchantId());
                if (merchant != null) {
                    dish.setMerchantName(merchant.getName());
                }
            }

            // 添加状态代码
            dish.setStatusCode(dish.getStatus() ? "ACTIVE" : "INACTIVE");

            response.put("success", true);
            response.put("dish", dish);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "菜品不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 修改菜品状态
     */
    @ApiOperation("修改菜品状态")
    @PutMapping("/{dishId}/status")
    @PreAuthorize("hasAnyAuthority('admin:dish:list')")
    public ResponseEntity<Map<String, Object>> updateDishStatus(
            @PathVariable String dishId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        Dish dish = dishService.getById(dishId);

        Map<String, Object> response = new HashMap<>();
        if (dish != null) {
            dish.setStatus("ACTIVE".equals(status));
            dishService.updateById(dish);

            response.put("success", true);
            response.put("message", "状态修改成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "菜品不存在");
            return ResponseEntity.status(404).body(response);
        }
    }
}
