package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.entity.MenuDish;
import com.xx.jaseatschoicejava.dto.MenuWithDishStatusDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService extends IService<Menu> {

    /**
     * 设置菜单自动上下架时间
     * @param menuId 菜单ID
     * @param autoStartTime 自动上架时间
     * @param autoEndTime 自动下架时间
     * @return 是否设置成功
     */
    boolean setMenuSchedule(String menuId, LocalDateTime autoStartTime, LocalDateTime autoEndTime);

    /**
     * 批量操作菜单
     * @param menuIds 菜单ID列表
     * @param action 操作类型：activate/deactivate/delete
     * @return 是否操作成功
     */
    boolean batchOperateMenus(List<String> menuIds, String action);

    /**
     * 审核"想吃列表"
     * @param itemId 想吃列表ID
     * @param status 审核状态：approved/rejected
     * @param remark 审核备注
     * @return 是否审核成功
     */
    boolean reviewWantToEat(Long itemId, String status, String remark);

    /**
     * 更新菜品在菜单中的状态
     * @param menuId 菜单ID
     * @param dishId 菜品ID
     * @param status 状态（1-上架/ 0-下架）
     * @return 是否更新成功
     */
    boolean updateDishStatusInMenu(String menuId, String dishId, Integer status);

    /**
     * 批量更新菜品在多个菜单中的状态
     * @param dishId 菜品ID
     * @param menuIds 菜单ID列表
     * @param status 状态（1-上架/ 0-下架）
     * @return 是否更新成功
     */
    boolean batchUpdateDishStatusInMenus(String dishId, List<String> menuIds, Integer status);

    /**
     * 获取菜品关联的所有菜单及状态
     * @param dishId 菜品ID
     * @return 菜单列表及菜品在该菜单中的状态
     */
    List<MenuWithDishStatusDTO> getMenusByDishId(String dishId);

    /**
     * 根据菜单ID查询菜品数量
     * @param menuId 菜单ID
     * @return 菜品数量
     */
    Integer getDishCountByMenuId(String menuId);
}
