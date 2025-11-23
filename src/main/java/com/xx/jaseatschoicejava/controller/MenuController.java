package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/v1")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 根据商家ID获取菜单列表
     */
    @GetMapping("/merchants/{merchantId}/menu")
    public ResponseResult<?> getMenusByMerchantId(@PathVariable Long merchantId) {
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Menu::getMerchantId, merchantId);
        queryWrapper.eq(Menu::getStatus, "active"); // 只显示激活状态的菜单
        List<Menu> menus = menuService.list(queryWrapper);
        return ResponseResult.success(menus);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/menus/{menuId}")
    public ResponseResult<?> getMenuDetail(@PathVariable Long menuId) {
        Menu menu = menuService.getById(menuId);
        if (menu != null) {
            return ResponseResult.success(menu);
        }
        return ResponseResult.fail("404", "菜单不存在");
    }

    /**
     * 设置菜单自动上下架时间
     */
    @PutMapping("/merchants/{merchantId}/menu/{menuId}/schedule")
    public ResponseResult<?> setMenuSchedule(@PathVariable Long merchantId, @PathVariable Long menuId, @RequestBody Map<String, Object> params) {
        try {
            LocalDateTime autoStartTime = LocalDateTime.parse((String) params.get("autoStartTime"));
            LocalDateTime autoEndTime = LocalDateTime.parse((String) params.get("autoEndTime"));
            boolean success = menuService.setMenuSchedule(menuId, autoStartTime, autoEndTime);
            if (success) {
                return ResponseResult.success("自动上下架时间设置成功");
            }
            return ResponseResult.fail("500", "自动上下架时间设置失败");
        } catch (DateTimeParseException e) {
            return ResponseResult.fail("400", "时间格式错误");
        }
    }

    /**
     * 菜单批量操作
     */
    @PostMapping("/merchants/{merchantId}/menu/batch")
    public ResponseResult<?> batchOperateMenus(@PathVariable Long merchantId, @RequestBody Map<String, Object> params) {
        List<Long> menuIds = (List<Long>) params.get("menuIds");
        String action = (String) params.get("action");
        boolean success = menuService.batchOperateMenus(menuIds, action);
        if (success) {
            return ResponseResult.success("批量操作成功");
        }
        return ResponseResult.fail("500", "批量操作失败");
    }
}
