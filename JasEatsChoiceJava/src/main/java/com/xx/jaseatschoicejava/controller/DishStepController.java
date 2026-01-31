package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.DishStepUpdateDTO;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.DishStepService;
import com.xx.jaseatschoicejava.vo.DishStepDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 菜品步骤管理控制器
 *
 * @author Claude
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/v1/dish-steps")
@Api(tags = "菜品步骤管理")
public class DishStepController {

    private static final Logger log = LoggerFactory.getLogger(DishStepController.class);

    @Autowired
    private DishStepService dishStepService;

    @PostMapping("/update")
    @ApiOperation("更新单个菜品步骤")
    public ResponseResult<Boolean> updateDishStep(@RequestBody DishStepUpdateDTO dto, HttpServletRequest request) {
        try {
            // 获取当前用户信息
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser != null) {
                // 这里可以设置操作人信息到DTO中
            }

            boolean success = dishStepService.updateDishStep(dto);
            return ResponseResult.success(success);
        } catch (Exception e) {
            log.error("更新菜品步骤失败", e);
            return ResponseResult.fail("500", "更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-update")
    @ApiOperation("批量更新菜品步骤")
    public ResponseResult<String> batchUpdateDishSteps(@RequestBody DishStepUpdateDTO dto, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            int count = dishStepService.batchUpdateDishSteps(dto);
            return ResponseResult.success("成功更新 " + count + " 个菜品步骤");
        } catch (Exception e) {
            log.error("批量更新菜品步骤失败", e);
            return ResponseResult.fail("500", "批量更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/rollback")
    @ApiOperation("回退菜品步骤")
    public ResponseResult<Boolean> rollbackDishStep(@RequestBody DishStepUpdateDTO dto) {
        try {
            boolean success = dishStepService.rollbackDishStep(dto);
            return ResponseResult.success(success);
        } catch (Exception e) {
            log.error("回退菜品步骤失败", e);
            return ResponseResult.fail("500", "回退失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-mark")
    @ApiOperation("批量标记菜品步骤")
    public ResponseResult<String> batchMarkDishSteps(
            @ApiParam("订单菜品ID列表") @RequestBody List<String> orderDishIds,
            @ApiParam("目标步骤状态") @RequestParam Integer targetStepStatus,
            HttpServletRequest request) {
        try {
            // TODO: 从request中获取当前用户信息
            String operatorId = "system";
            String operatorName = "系统";

            int count = dishStepService.batchMarkDishSteps(orderDishIds, targetStepStatus, operatorId, operatorName);
            return ResponseResult.success("成功标记 " + count + " 个菜品");
        } catch (Exception e) {
            log.error("批量标记菜品步骤失败", e);
            return ResponseResult.fail("500", "批量标记失败：" + e.getMessage());
        }
    }

    @PostMapping("/update-sort")
    @ApiOperation("更新菜品步骤排序")
    public ResponseResult<Boolean> updateStepSort(@RequestBody List<DishStepUpdateDTO.StepSortItem> stepSortItems) {
        try {
            boolean success = dishStepService.updateStepSort(stepSortItems);
            return ResponseResult.success(success);
        } catch (Exception e) {
            log.error("更新菜品步骤排序失败", e);
            return ResponseResult.fail("500", "更新排序失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/{orderDishId}")
    @ApiOperation("获取菜品步骤详情")
    public ResponseResult<DishStepDetailVO> getDishStepDetail(
            @ApiParam("订单菜品ID") @PathVariable String orderDishId) {
        try {
            DishStepDetailVO detail = dishStepService.getDishStepDetail(orderDishId);
            return ResponseResult.success(detail);
        } catch (Exception e) {
            log.error("获取菜品步骤详情失败", e);
            return ResponseResult.fail("500", "获取详情失败：" + e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    @ApiOperation("获取订单所有菜品的步骤详情")
    public ResponseResult<List<DishStepDetailVO>> getOrderDishSteps(
            @ApiParam("订单ID") @PathVariable String orderId) {
        try {
            List<DishStepDetailVO> steps = dishStepService.getOrderDishSteps(orderId);
            return ResponseResult.success(steps);
        } catch (Exception e) {
            log.error("获取订单菜品步骤失败", e);
            return ResponseResult.fail("500", "获取步骤列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/filter")
    @ApiOperation("根据步骤状态筛选订单菜品")
    public ResponseResult<List<DishStepDetailVO>> filterByStepStatus(
            @ApiParam("订单ID") @RequestParam String orderId,
            @ApiParam("步骤状态") @RequestParam Integer stepStatus) {
        try {
            List<DishStepDetailVO> steps = dishStepService.filterByStepStatus(orderId, stepStatus);
            return ResponseResult.success(steps);
        } catch (Exception e) {
            log.error("筛选菜品步骤失败", e);
            return ResponseResult.fail("500", "筛选失败：" + e.getMessage());
        }
    }

    @PostMapping("/initialize/{orderId}")
    @ApiOperation("初始化订单菜品步骤（订单创建时调用）")
    public ResponseResult<Boolean> initializeOrderDishSteps(
            @ApiParam("订单ID") @PathVariable String orderId) {
        try {
            boolean success = dishStepService.initializeOrderDishSteps(orderId);
            return ResponseResult.success(success);
        } catch (Exception e) {
            log.error("初始化订单菜品步骤失败", e);
            return ResponseResult.fail("500", "初始化失败：" + e.getMessage());
        }
    }
}
