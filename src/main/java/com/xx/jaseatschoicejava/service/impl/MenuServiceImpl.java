package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.mapper.MenuMapper;
import com.xx.jaseatschoicejava.service.MenuService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单服务实现
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public boolean setMenuSchedule(Long menuId, LocalDateTime autoStartTime, LocalDateTime autoEndTime) {
        Menu menu = getById(menuId);
        if (menu == null) {
            return false;
        }
        menu.setAutoStartTime(autoStartTime);
        menu.setAutoEndTime(autoEndTime);
        return updateById(menu);
    }

    @Override
    public boolean batchOperateMenus(List<Long> menuIds, String action) {
        Menu updateMenu = new Menu();
        switch (action) {
            case "activate":
                updateMenu.setStatus("active");
                break;
            case "deactivate":
                updateMenu.setStatus("inactive");
                break;
            case "delete":
                return removeByIds(menuIds);
            default:
                return false;
        }
        return update(updateMenu, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Menu>().in("id", menuIds));
    }

    @Override
    public boolean reviewWantToEat(Long itemId, String status, String remark) {
        // TODO: Implement reviewWantToEat logic
        // This feature requires a want_to_eat table which isn't implemented yet
        return false;
    }
}
