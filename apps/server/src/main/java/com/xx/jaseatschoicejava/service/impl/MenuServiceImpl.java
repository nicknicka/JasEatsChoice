package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.entity.MenuDish;
import com.xx.jaseatschoicejava.mapper.MenuMapper;
import com.xx.jaseatschoicejava.mapper.MenuDishMapper;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.xx.jaseatschoicejava.dto.MenuWithDishStatusDTO;
import java.util.List;

/**
 * 菜单服务实现
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Autowired
    private MenuDishMapper menuDishMapper;

    @Autowired
    private DishService dishService;

    @Override
    public boolean setMenuSchedule(String menuId, LocalDateTime autoStartTime, LocalDateTime autoEndTime) {
        Menu menu = getById(menuId);
        if (menu == null) {
            return false;
        }
        menu.setAutoStartTime(autoStartTime);
        menu.setAutoEndTime(autoEndTime);
        return updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchOperateMenus(List<String> menuIds, String action, String merchantId) {
        if (menuIds == null || menuIds.isEmpty() || action == null) {
            return false;
        }

        if ("delete".equals(action)) {
            if (merchantId == null || merchantId.isBlank()) {
                return false;
            }
            List<Menu> menus = list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Menu>()
                    .in(Menu::getId, menuIds)
                    .eq(Menu::getMerchantId, merchantId));
            if (menus.size() != menuIds.stream().distinct().count()) {
                return false;
            }

            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MenuDish> menuDishWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            menuDishWrapper.in(MenuDish::getMenuId, menuIds);
            menuDishMapper.delete(menuDishWrapper);
            return removeByIds(menuIds);
        }

        Menu updateMenu = new Menu();
        switch (action) {
            case "activate":
                updateMenu.setStatus("active");
                break;
            case "deactivate":
                updateMenu.setStatus("inactive");
                break;
            default:
                return false;
        }
        return update(updateMenu, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Menu>().in("id", menuIds));
    }

    @Override
    public boolean updateDishStatusInMenu(String menuId, String dishId, Integer status) {
        // 检查输入参数
        if (menuId == null || menuId.isEmpty() || dishId == null || dishId.isEmpty() || status == null) {
            return false;
        }

        // 当要上架菜品时，检查该菜品在菜品管理中的状态
        if (status == 1) {
            Dish dish = dishService.getById(dishId);
            if (dish == null || !Boolean.TRUE.equals(dish.getStatus())) {
                // 菜品不存在或菜品已下架，不允许上架
                return false;
            }
        }

        // 创建更新条件
        UpdateWrapper<MenuDish> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("menu_id", menuId).eq("dish_id", dishId);

        // 更新菜品状态
        MenuDish menuDish = new MenuDish();
        menuDish.setStatus(status);

        // 执行更新操作
        return menuDishMapper.update(menuDish, updateWrapper) > 0;
    }

    @Override
    public boolean batchUpdateDishStatusInMenus(String dishId, List<String> menuIds, Integer status) {
        // 检查输入参数
        if (dishId == null || dishId.isEmpty() || menuIds == null || menuIds.isEmpty() || status == null) {
            return false;
        }

        // 创建更新条件
        UpdateWrapper<MenuDish> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("dish_id", dishId).in("menu_id", menuIds);

        // 更新菜品状态
        MenuDish menuDish = new MenuDish();
        menuDish.setStatus(status);

        // 执行更新操作
        return menuDishMapper.update(menuDish, updateWrapper) > 0;
    }

    @Override
    public List<MenuWithDishStatusDTO> getMenusByDishId(String dishId) {
        // 这里应该通过MenuDishMapper查询该菜品关联的所有菜单
        // 并返回菜单的基本信息，包括菜单ID、名称、类型、状态以及菜品在该菜单中的状态
        // 由于需要关联查询，可能需要在MenuDishMapper中添加自定义SQL
        return menuDishMapper.selectMenusByDishId(dishId);
    }

    @Override
    public Integer getDishCountByMenuId(String menuId) {
        return menuDishMapper.selectDishCountByMenuId(menuId);
    }
}
