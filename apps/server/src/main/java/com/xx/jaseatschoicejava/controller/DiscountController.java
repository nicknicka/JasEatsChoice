
package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Discount;
import com.xx.jaseatschoicejava.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 优惠活动控制器
 */
@RestController
@RequestMapping("/v1/merchant")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    /**
     * 获取商家的优惠活动列表
     */
    @GetMapping("/{merchantId}/discounts")
    public ResponseResult<?> getMerchantDiscounts(@PathVariable String merchantId) {
        // 根据商家ID查询所有优惠活动
        LambdaQueryWrapper<Discount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Discount::getMerchantId, merchantId);

        List<Discount> discounts = discountService.list(queryWrapper);
        return ResponseResult.success(discounts);
    }

    /**
     * 添加优惠活动
     */
    @PostMapping("/{merchantId}/discounts")
    public ResponseResult<?> addDiscount(@PathVariable String merchantId, @RequestBody Discount discount) {
        discount.setMerchantId(merchantId);
        boolean success = discountService.save(discount);
        if (success) {
            // 重新查询数据库以获取完整的字段值（包括自动填充的createTime和updateTime）
            Discount savedDiscount = discountService.getById(discount.getId());
            return ResponseResult.success(savedDiscount, "优惠活动添加成功");
        }
        return ResponseResult.fail("500", "优惠活动添加失败");
    }

    /**
     * 更新优惠活动
     */
    @PutMapping("/{merchantId}/discounts/{discountId}")
    public ResponseResult<?> updateDiscount(
            @PathVariable String merchantId,
            @PathVariable String discountId,
            @RequestBody Discount discount) {
        // 验证优惠活动属于该商家
        Discount existingDiscount = discountService.getById(discountId);
        if (existingDiscount == null || !existingDiscount.getMerchantId().equals(merchantId)) {
            return ResponseResult.fail("404", "优惠活动不存在");
        }

        // 确保ID一致
        discount.setId(discountId);

        boolean success = discountService.updateById(discount);
        if (success) {
            return ResponseResult.success(discount, "优惠活动更新成功");
        }
        return ResponseResult.fail("500", "优惠活动更新失败");
    }

    /**
     * 批量更新优惠状态
     */
    @PutMapping("/{merchantId}/discounts/batch")
    public ResponseResult<?> batchUpdateDiscountStatus(
            @PathVariable String merchantId,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> discountIds = (List<String>) request.get("discountIds");
        String status = (String) request.get("status");

        if (discountIds == null || discountIds.isEmpty()) {
            return ResponseResult.fail("400", "请选择要更新的优惠活动");
        }

        // 验证所有优惠活动都属于该商家
        LambdaQueryWrapper<Discount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Discount::getMerchantId, merchantId)
                .in(Discount::getId, discountIds);
        long count = discountService.count(queryWrapper);

        if (count != discountIds.size()) {
            return ResponseResult.fail("400", "部分优惠活动不存在或不属于该商家");
        }

        // 批量更新状态
        LambdaUpdateWrapper<Discount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Discount::getId, discountIds).set(Discount::getStatus, status);
        boolean success = discountService.update(updateWrapper);
        if (success) {
            return ResponseResult.success(null, "批量更新成功");
        }
        return ResponseResult.fail("500", "批量更新失败");
    }

    /**
     * 切换单个优惠活动状态
     */
    @PutMapping("/{merchantId}/discounts/{discountId}/status")
    public ResponseResult<?> toggleDiscountStatus(
            @PathVariable String merchantId,
            @PathVariable String discountId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");

        // 验证优惠活动属于该商家
        Discount discount = discountService.getById(discountId);
        if (discount == null || !discount.getMerchantId().equals(merchantId)) {
            return ResponseResult.fail("404", "优惠活动不存在");
        }

        // 更新状态
        discount.setStatus(status);
        boolean success = discountService.updateById(discount);
        if (success) {
            return ResponseResult.success(discount, "状态更新成功");
        }
        return ResponseResult.fail("500", "状态更新失败");
    }

    /**
     * 删除优惠活动
     */
    @DeleteMapping("/{merchantId}/discounts/{discountId}")
    public ResponseResult<?> deleteDiscount(
            @PathVariable String merchantId,
            @PathVariable String discountId) {
        // 验证优惠活动属于该商家
        Discount discount = discountService.getById(discountId);
        if (discount == null || !discount.getMerchantId().equals(merchantId)) {
            return ResponseResult.fail("404", "优惠活动不存在");
        }

        boolean success = discountService.removeById(discountId);
        if (success) {
            return ResponseResult.success(null, "优惠活动删除成功");
        }
        return ResponseResult.fail("500", "优惠活动删除失败");
    }

    /**
     * 批量删除优惠活动
     */
    @DeleteMapping("/{merchantId}/discounts/batch")
    public ResponseResult<?> batchDeleteDiscounts(
            @PathVariable String merchantId,
            @RequestParam String ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseResult.fail("400", "请选择要删除的优惠活动");
        }

        // 将逗号分隔的字符串转换为列表
        List<String> discountIds = java.util.Arrays.asList(ids.split(","));

        // 验证所有优惠活动都属于该商家
        LambdaQueryWrapper<Discount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Discount::getMerchantId, merchantId)
                .in(Discount::getId, discountIds);
        long count = discountService.count(queryWrapper);

        if (count != discountIds.size()) {
            return ResponseResult.fail("400", "部分优惠活动不存在或不属于该商家");
        }

        // 批量删除
        boolean success = discountService.removeByIds(discountIds);
        if (success) {
            return ResponseResult.success(null, "批量删除成功");
        }
        return ResponseResult.fail("500", "批量删除失败");
    }
}
