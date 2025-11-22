package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 根据商家ID获取菜单列表
     */
    @GetMapping("/merchant/{merchantId}")
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
    @GetMapping("/{menuId}")
    public ResponseResult<?> getMenuDetail(@PathVariable Long menuId) {
        Menu menu = menuService.getById(menuId);
        if (menu != null) {
            return ResponseResult.success(menu);
        }
        return ResponseResult.fail("404", "菜单不存在");
    }
}
