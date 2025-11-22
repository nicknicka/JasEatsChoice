package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家控制器
 */
@RestController
@RequestMapping("/merchants")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取商家列表
     */
    @GetMapping
    public ResponseResult<?> getMerchants(@RequestParam(required = false) String category,
                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            queryWrapper.eq(Merchant::getCategory, category);
        }
        if (keyword != null) {
            queryWrapper.like(Merchant::getName, keyword);
        }
        // 只显示营业的商家
        queryWrapper.eq(Merchant::getStatus, true);
        List<Merchant> merchants = merchantService.list(queryWrapper);
        return ResponseResult.success(merchants);
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{merchantId}")
    public ResponseResult<?> getMerchantDetail(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant != null) {
            return ResponseResult.success(merchant);
        }
        return ResponseResult.fail("404", "商家不存在");
    }
}
