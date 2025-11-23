package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Menu;

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
    boolean setMenuSchedule(Long menuId, LocalDateTime autoStartTime, LocalDateTime autoEndTime);

    /**
     * 批量操作菜单
     * @param menuIds 菜单ID列表
     * @param action 操作类型：activate/deactivate/delete
     * @return 是否操作成功
     */
    boolean batchOperateMenus(List<Long> menuIds, String action);

    /**
     * 审核"想吃列表"
     * @param itemId 想吃列表ID
     * @param status 审核状态：approved/rejected
     * @param remark 审核备注
     * @return 是否审核成功
     */
    boolean reviewWantToEat(Long itemId, String status, String remark);
}
