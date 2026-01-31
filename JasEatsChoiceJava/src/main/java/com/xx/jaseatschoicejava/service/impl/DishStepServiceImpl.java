package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.jaseatschoicejava.dto.DishStepUpdateDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.DishStepHistory;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.enums.DishStepStatus;
import com.xx.jaseatschoicejava.mapper.DishMapper;
import com.xx.jaseatschoicejava.mapper.DishStepHistoryMapper;
import com.xx.jaseatschoicejava.mapper.OrderDishMapper;
import com.xx.jaseatschoicejava.service.DishStepService;
import com.xx.jaseatschoicejava.vo.DishStepDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜品步骤服务实现类
 *
 * @author Claude
 * @since 2025-01-30
 */
@Slf4j
@Service
public class DishStepServiceImpl implements DishStepService {

    @Autowired
    private OrderDishMapper orderDishMapper;

    @Autowired
    private DishStepHistoryMapper dishStepHistoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDishStep(DishStepUpdateDTO dto) {
        if (dto.getOrderDishId() == null || dto.getNewStepStatus() == null) {
            throw new IllegalArgumentException("订单菜品ID和新步骤状态不能为空");
        }

        // 查询订单菜品
        OrderDish orderDish = orderDishMapper.selectById(dto.getOrderDishId());
        if (orderDish == null) {
            throw new RuntimeException("订单菜品不存在");
        }

        // 验证步骤状态是否合法
        DishStepStatus newStatus = DishStepStatus.getByCode(dto.getNewStepStatus());
        if (newStatus == null) {
            throw new IllegalArgumentException("无效的步骤状态");
        }

        // 更新订单菜品步骤
        Integer oldStepStatus = orderDish.getStepStatus();
        orderDish.setStepStatus(dto.getNewStepStatus());
        orderDish.setStepStartTime(LocalDateTime.now());

        // 计算预计完成时间
        if (dto.getEstimatedMinutes() != null && dto.getEstimatedMinutes() > 0) {
            orderDish.setEstimatedCompletionTime(LocalDateTime.now().plusMinutes(dto.getEstimatedMinutes()));
        }

        int updated = orderDishMapper.updateById(orderDish);
        if (updated <= 0) {
            throw new RuntimeException("更新菜品步骤失败");
        }

        // 记录步骤历史
        recordStepHistory(orderDish, oldStepStatus, dto.getNewStepStatus(),
            dto.getOperationType() != null ? dto.getOperationType() : "FORWARD",
            dto.getRollbackReason(), dto.getRemark(), dto.getEstimatedMinutes());

        log.info("更新菜品步骤成功：orderDishId={}, oldStep={}, newStep={}",
            dto.getOrderDishId(), oldStepStatus, dto.getNewStepStatus());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDishSteps(DishStepUpdateDTO dto) {
        if (CollectionUtils.isEmpty(dto.getOrderDishIds()) || dto.getNewStepStatus() == null) {
            throw new IllegalArgumentException("订单菜品ID列表和新步骤状态不能为空");
        }

        int successCount = 0;
        for (String orderDishId : dto.getOrderDishIds()) {
            try {
                DishStepUpdateDTO singleDto = new DishStepUpdateDTO();
                singleDto.setOrderDishId(orderDishId);
                singleDto.setNewStepStatus(dto.getNewStepStatus());
                singleDto.setOperationType(dto.getOperationType());
                singleDto.setRemark(dto.getRemark());
                singleDto.setEstimatedMinutes(dto.getEstimatedMinutes());

                if (updateDishStep(singleDto)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量更新菜品步骤失败：orderDishId={}, error={}", orderDishId, e.getMessage());
            }
        }

        log.info("批量更新菜品步骤完成：total={}, success={}", dto.getOrderDishIds().size(), successCount);
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackDishStep(DishStepUpdateDTO dto) {
        if (dto.getOrderDishId() == null || dto.getNewStepStatus() == null) {
            throw new IllegalArgumentException("订单菜品ID和目标步骤状态不能为空");
        }

        // 验证回退原因
        if (dto.getRollbackReason() == null || dto.getRollbackReason().trim().isEmpty()) {
            throw new IllegalArgumentException("回退操作必须填写原因");
        }

        // 查询当前菜品步骤
        OrderDish orderDish = orderDishMapper.selectById(dto.getOrderDishId());
        if (orderDish == null) {
            throw new RuntimeException("订单菜品不存在");
        }

        // 验证是否可以回退到目标步骤
        DishStepStatus currentStatus = DishStepStatus.getByCode(orderDish.getStepStatus());
        DishStepStatus targetStatus = DishStepStatus.getByCode(dto.getNewStepStatus());

        if (currentStatus == null || targetStatus == null) {
            throw new IllegalArgumentException("无效的步骤状态");
        }

        DishStepStatus[] rollbackSteps = currentStatus.getRollbackSteps();
        boolean canRollback = Arrays.stream(rollbackSteps)
            .anyMatch(step -> step.getCode().equals(dto.getNewStepStatus()));

        if (!canRollback) {
            throw new RuntimeException("不能从当前步骤回退到目标步骤");
        }

        dto.setOperationType("BACKWARD");
        return updateDishStep(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkDishSteps(List<String> orderDishIds, Integer targetStepStatus,
                                   String operatorId, String operatorName) {
        if (CollectionUtils.isEmpty(orderDishIds) || targetStepStatus == null) {
            throw new IllegalArgumentException("订单菜品ID列表和目标步骤状态不能为空");
        }

        int successCount = 0;
        for (String orderDishId : orderDishIds) {
            try {
                DishStepUpdateDTO dto = new DishStepUpdateDTO();
                dto.setOrderDishId(orderDishId);
                dto.setNewStepStatus(targetStepStatus);
                dto.setOperationType("FORWARD");
                dto.setRemark("批量标记操作");

                if (updateDishStep(dto)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量标记菜品步骤失败：orderDishId={}, error={}", orderDishId, e.getMessage());
            }
        }

        log.info("批量标记菜品步骤完成：operator={}, total={}, success={}",
            operatorName, orderDishIds.size(), successCount);
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStepSort(List<DishStepUpdateDTO.StepSortItem> stepSortItems) {
        if (CollectionUtils.isEmpty(stepSortItems)) {
            throw new IllegalArgumentException("步骤排序项列表不能为空");
        }

        int successCount = 0;
        for (DishStepUpdateDTO.StepSortItem item : stepSortItems) {
            try {
                LambdaUpdateWrapper<OrderDish> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(OrderDish::getId, item.getOrderDishId())
                    .set(OrderDish::getStepSort, item.getSort());

                if (orderDishMapper.update(null, updateWrapper) > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("更新菜品步骤排序失败：orderDishId={}, error={}", item.getOrderDishId(), e.getMessage());
            }
        }

        log.info("更新菜品步骤排序完成：total={}, success={}", stepSortItems.size(), successCount);
        return successCount > 0;
    }

    @Override
    public DishStepDetailVO getDishStepDetail(String orderDishId) {
        // 查询订单菜品
        OrderDish orderDish = orderDishMapper.selectById(orderDishId);
        if (orderDish == null) {
            throw new RuntimeException("订单菜品不存在");
        }

        // 查询菜品信息
        Dish dish = dishMapper.selectById(orderDish.getDishId());
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }

        // 查询步骤历史
        List<DishStepHistory> historyList = dishStepHistoryMapper.listByOrderDishId(orderDishId);

        // 构建VO
        DishStepDetailVO vo = buildDishStepDetailVO(orderDish, dish, historyList);
        return vo;
    }

    @Override
    public List<DishStepDetailVO> getOrderDishSteps(String orderId) {
        // 查询订单所有菜品
        LambdaQueryWrapper<OrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDish::getOrderId, orderId)
            .orderByAsc(OrderDish::getStepSort);

        List<OrderDish> orderDishList = orderDishMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(orderDishList)) {
            return Collections.emptyList();
        }

        // 查询菜品信息
        Set<String> dishIds = orderDishList.stream()
            .map(OrderDish::getDishId)
            .collect(Collectors.toSet());
        Map<String, Dish> dishMap = dishMapper.selectBatchIds(dishIds).stream()
            .collect(Collectors.toMap(Dish::getId, d -> d));

        // 查询步骤历史
        List<DishStepHistory> allHistory = dishStepHistoryMapper.listByOrderId(orderId);
        Map<String, List<DishStepHistory>> historyMap = allHistory.stream()
            .collect(Collectors.groupingBy(DishStepHistory::getOrderDishId));

        // 构建VO列表
        List<DishStepDetailVO> voList = new ArrayList<>();
        for (OrderDish orderDish : orderDishList) {
            Dish dish = dishMap.get(orderDish.getDishId());
            List<DishStepHistory> historyList = historyMap.getOrDefault(orderDish.getId(), Collections.emptyList());
            voList.add(buildDishStepDetailVO(orderDish, dish, historyList));
        }

        return voList;
    }

    @Override
    public List<DishStepDetailVO> filterByStepStatus(String orderId, Integer stepStatus) {
        List<DishStepDetailVO> allSteps = getOrderDishSteps(orderId);
        return allSteps.stream()
            .filter(vo -> stepStatus.equals(vo.getStepStatus()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoAdvanceDishSteps() {
        // 查询所有未完成的菜品
        LambdaQueryWrapper<OrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderDish::getStepStatus,
            DishStepStatus.PREPARING.getCode(),
            DishStepStatus.PRE_PROCESSING.getCode(),
            DishStepStatus.COOKING.getCode(),
            DishStepStatus.PLATING.getCode()
        );

        List<OrderDish> orderDishList = orderDishMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(orderDishList)) {
            return 0;
        }

        int advancedCount = 0;
        for (OrderDish orderDish : orderDishList) {
            try {
                // 检查是否超过预计完成时间
                if (orderDish.getEstimatedCompletionTime() != null &&
                    LocalDateTime.now().isAfter(orderDish.getEstimatedCompletionTime())) {

                    DishStepStatus currentStatus = DishStepStatus.getByCode(orderDish.getStepStatus());
                    DishStepStatus nextStatus = currentStatus.getNextStep();

                    if (nextStatus != null) {
                        DishStepUpdateDTO dto = new DishStepUpdateDTO();
                        dto.setOrderDishId(orderDish.getId());
                        dto.setNewStepStatus(nextStatus.getCode());
                        dto.setOperationType("FORWARD");
                        dto.setRemark("系统自动推进");

                        if (updateDishStep(dto)) {
                            advancedCount++;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("自动推进菜品步骤失败：orderDishId={}, error={}",
                    orderDish.getId(), e.getMessage());
            }
        }

        log.info("自动推进菜品步骤完成：advanced={}", advancedCount);
        return advancedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initializeOrderDishSteps(String orderId) {
        // 查询订单所有菜品
        LambdaQueryWrapper<OrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDish::getOrderId, orderId);
        List<OrderDish> orderDishList = orderDishMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(orderDishList)) {
            return true;
        }

        // 查询菜品信息获取烹饪时长
        Set<String> dishIds = orderDishList.stream()
            .map(OrderDish::getDishId)
            .collect(Collectors.toSet());
        Map<String, Dish> dishMap = dishMapper.selectBatchIds(dishIds).stream()
            .collect(Collectors.toMap(Dish::getId, d -> d));

        int successCount = 0;
        for (OrderDish orderDish : orderDishList) {
            try {
                Dish dish = dishMap.get(orderDish.getDishId());
                if (dish == null) {
                    continue;
                }

                // 初始化步骤状态
                orderDish.setStepStatus(DishStepStatus.PENDING_PREPARATION.getCode());
                orderDish.setStepStartTime(LocalDateTime.now());
                orderDish.setIsFastFood(dish.getIsFastFood() != null ? dish.getIsFastFood() : false);
                orderDish.setStepSort(999); // 默认排序

                // 设置烹饪时长
                Integer cookingMinutes = dish.getCookingMinutes() != null ? dish.getCookingMinutes() : 15;
                orderDish.setCookingMinutes(cookingMinutes);

                orderDishMapper.updateById(orderDish);
                successCount++;

            } catch (Exception e) {
                log.error("初始化订单菜品步骤失败：orderDishId={}, error={}",
                    orderDish.getId(), e.getMessage());
            }
        }

        log.info("初始化订单菜品步骤完成：orderId={}, total={}, success={}",
            orderId, orderDishList.size(), successCount);
        return successCount > 0;
    }

    /**
     * 记录步骤历史
     */
    private void recordStepHistory(OrderDish orderDish, Integer oldStepStatus, Integer newStepStatus,
                                   String operationType, String rollbackReason, String remark, Integer estimatedMinutes) {
        DishStepHistory history = new DishStepHistory();
        history.setId(UUID.randomUUID().toString().replace("-", ""));
        history.setOrderDishId(orderDish.getId());
        history.setOrderId(orderDish.getOrderId());
        history.setDishId(orderDish.getDishId());
        history.setOldStepStatus(oldStepStatus);
        history.setNewStepStatus(newStepStatus);
        history.setOperationType(operationType);
        history.setRollbackReason(rollbackReason);
        history.setRemark(remark);
        history.setEstimatedMinutes(estimatedMinutes);
        history.setCreateTime(LocalDateTime.now());

        dishStepHistoryMapper.insert(history);
    }

    /**
     * 构建菜品步骤详情VO
     */
    private DishStepDetailVO buildDishStepDetailVO(OrderDish orderDish, Dish dish, List<DishStepHistory> historyList) {
        DishStepDetailVO vo = new DishStepDetailVO();
        vo.setOrderDishId(orderDish.getId());
        vo.setOrderId(orderDish.getOrderId());
        vo.setDishId(orderDish.getDishId());
        vo.setDishName(dish.getName());
        vo.setDishImage(dish.getImage());
        vo.setQuantity(orderDish.getQuantity());
        vo.setStepStatus(orderDish.getStepStatus());

        DishStepStatus status = DishStepStatus.getByCode(orderDish.getStepStatus());
        vo.setStepStatusName(status != null ? status.getDescription() : "未知");

        vo.setStepStartTime(orderDish.getStepStartTime());
        vo.setEstimatedCompletionTime(orderDish.getEstimatedCompletionTime());
        vo.setCookingMinutes(orderDish.getCookingMinutes());
        vo.setStepSort(orderDish.getStepSort());
        vo.setIsFastFood(orderDish.getIsFastFood());
        vo.setServingStatus(orderDish.getServingStatus());

        // 计算已用时间和剩余时间
        if (orderDish.getStepStartTime() != null) {
            long elapsed = ChronoUnit.MINUTES.between(orderDish.getStepStartTime(), LocalDateTime.now());
            vo.setElapsedMinutes(elapsed);

            if (orderDish.getEstimatedCompletionTime() != null) {
                long remaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), orderDish.getEstimatedCompletionTime());
                vo.setRemainingMinutes(Math.max(0, remaining));
            }
        }

        // 计算进度百分比
        if (orderDish.getStepStatus() != null) {
            int totalSteps = orderDish.getIsFastFood() ? 4 : 7;
            int currentStep = orderDish.getStepStatus() >= 10 ? orderDish.getStepStatus() - 9 : orderDish.getStepStatus() + 1;
            vo.setProgressPercent((currentStep * 100) / totalSteps);
        }

        // 构建步骤历史
        List<DishStepDetailVO.StepHistoryItem> historyItems = historyList.stream()
            .map(this::buildStepHistoryItem)
            .collect(Collectors.toList());
        vo.setStepHistory(historyItems);

        return vo;
    }

    /**
     * 构建步骤历史项
     */
    private DishStepDetailVO.StepHistoryItem buildStepHistoryItem(DishStepHistory history) {
        DishStepDetailVO.StepHistoryItem item = new DishStepDetailVO.StepHistoryItem();
        item.setId(history.getId());
        item.setOldStepStatus(history.getOldStepStatus());
        item.setNewStepStatus(history.getNewStepStatus());
        item.setOperationType(history.getOperationType());
        item.setOperatorId(history.getOperatorId());
        item.setOperatorName(history.getOperatorName());
        item.setRollbackReason(history.getRollbackReason());
        item.setCreateTime(history.getCreateTime());
        item.setEstimatedMinutes(history.getEstimatedMinutes());

        DishStepStatus oldStatus = DishStepStatus.getByCode(history.getOldStepStatus());
        item.setOldStepStatusName(oldStatus != null ? oldStatus.getDescription() : "未知");

        DishStepStatus newStatus = DishStepStatus.getByCode(history.getNewStepStatus());
        item.setNewStepStatusName(newStatus != null ? newStatus.getDescription() : "未知");

        return item;
    }
}
