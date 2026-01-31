package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.DishStepUpdateDTO;
import com.xx.jaseatschoicejava.vo.DishStepDetailVO;

import java.util.List;

/**
 * 菜品步骤服务接口
 *
 * @author Claude
 * @since 2025-01-30
 */
public interface DishStepService {

    /**
     * 更新单个菜品步骤
     *
     * @param dto 步骤更新DTO
     * @return 是否成功
     */
    boolean updateDishStep(DishStepUpdateDTO dto);

    /**
     * 批量更新菜品步骤
     *
     * @param dto 步骤更新DTO
     * @return 更新成功的数量
     */
    int batchUpdateDishSteps(DishStepUpdateDTO dto);

    /**
     * 回退菜品步骤
     *
     * @param dto 步骤更新DTO
     * @return 是否成功
     */
    boolean rollbackDishStep(DishStepUpdateDTO dto);

    /**
     * 批量标记菜品步骤（勾选多个菜品统一标记到某一步骤）
     *
     * @param orderDishIds 订单菜品ID列表
     * @param targetStepStatus 目标步骤状态
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新成功的数量
     */
    int batchMarkDishSteps(List<String> orderDishIds, Integer targetStepStatus, String operatorId, String operatorName);

    /**
     * 更新菜品步骤排序
     *
     * @param stepSortItems 步骤排序项列表
     * @return 是否成功
     */
    boolean updateStepSort(List<DishStepUpdateDTO.StepSortItem> stepSortItems);

    /**
     * 获取订单菜品的步骤详情
     *
     * @param orderDishId 订单菜品ID
     * @return 步骤详情
     */
    DishStepDetailVO getDishStepDetail(String orderDishId);

    /**
     * 获取订单所有菜品的步骤详情
     *
     * @param orderId 订单ID
     * @return 步骤详情列表
     */
    List<DishStepDetailVO> getOrderDishSteps(String orderId);

    /**
     * 根据步骤状态筛选订单菜品
     *
     * @param orderId 订单ID
     * @param stepStatus 步骤状态
     * @return 步骤详情列表
     */
    List<DishStepDetailVO> filterByStepStatus(String orderId, Integer stepStatus);

    /**
     * 自动推进菜品步骤（定时任务调用）
     *
     * @return 推进的菜品数量
     */
    int autoAdvanceDishSteps();

    /**
     * 初始化订单菜品步骤（订单创建时调用）
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean initializeOrderDishSteps(String orderId);
}
