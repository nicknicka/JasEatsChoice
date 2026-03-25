package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.WishListAppealDTO;
import com.xx.jaseatschoicejava.dto.WishListAuditDTO;
import com.xx.jaseatschoicejava.dto.WishListItemCreateDTO;
import com.xx.jaseatschoicejava.service.WishListService;
import com.xx.jaseatschoicejava.vo.WishListItemDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 想吃列表控制器
 *
 * @author Claude
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/v1/wish-list")
public class WishListController {

    private static final Logger log = LoggerFactory.getLogger(WishListController.class);

    @Autowired
    private WishListService wishListService;

    /**
     * 创建想吃列表项
     *
     * @param dto 创建DTO
     * @param request HTTP请求
     * @return 列表项ID
     */
    @PostMapping("/item")
    public ResponseResult<String> createWishListItem(
            @RequestBody WishListItemCreateDTO dto,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        String itemId = wishListService.createWishListItem(dto, userId);
        return ResponseResult.success(itemId);
    }

    /**
     * 获取用户的想吃列表
     *
     * @param request HTTP请求
     * @return 想吃列表
     */
    @GetMapping("/items")
    public ResponseResult<List<WishListItemDetailVO>> getUserWishList(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        List<WishListItemDetailVO> list = wishListService.getUserWishList(userId);
        return ResponseResult.success(list);
    }

    /**
     * 获取商家待审核列表
     *
     * @param request HTTP请求
     * @return 待审核列表
     */
    @GetMapping("/merchant/pending")
    public ResponseResult<List<WishListItemDetailVO>> getMerchantPendingList(HttpServletRequest request) {
        String merchantId = (String) request.getAttribute("merchantId");
        if (merchantId == null) {
            merchantId = "test_merchant";
        }

        List<WishListItemDetailVO> list = wishListService.getMerchantPendingList(merchantId);
        return ResponseResult.success(list);
    }

    /**
     * 审核想吃列表项
     *
     * @param dto 审核DTO
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/audit")
    public ResponseResult<Boolean> auditWishListItem(
            @RequestBody WishListAuditDTO dto,
            HttpServletRequest request) {
        String auditorId = (String) request.getAttribute("userId");
        String auditorName = (String) request.getAttribute("username");

        if (auditorId == null) {
            auditorId = "test_user";
        }
        if (auditorName == null) {
            auditorName = "测试用户";
        }

        boolean success = wishListService.auditWishListItem(dto, auditorId, auditorName);
        return ResponseResult.success(success);
    }

    /**
     * 用户申诉
     *
     * @param dto 申诉DTO
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/appeal")
    public ResponseResult<Boolean> appealWishListItem(
            @RequestBody WishListAppealDTO dto,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        boolean success = wishListService.appealWishListItem(dto, userId);
        return ResponseResult.success(success);
    }

    /**
     * 撤回想吃列表项
     *
     * @param itemId 列表项ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @DeleteMapping("/item/{itemId}")
    public ResponseResult<Boolean> withdrawWishListItem(
            @PathVariable String itemId,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        boolean success = wishListService.withdrawWishListItem(itemId, userId);
        return ResponseResult.success(success);
    }

    /**
     * 获取列表项详情
     *
     * @param itemId 列表项ID
     * @param request HTTP请求
     * @return 详情VO
     */
    @GetMapping("/item/{itemId}")
    public ResponseResult<WishListItemDetailVO> getWishListItemDetail(
            @PathVariable String itemId,
            HttpServletRequest request) {
        WishListItemDetailVO detail = wishListService.getWishListItemDetail(itemId);
        return ResponseResult.success(detail);
    }

    /**
     * 自动审核超时项（定时任务调用）
     *
     * @return 处理数量
     */
    @PostMapping("/auto-audit-timeout")
    public ResponseResult<Integer> autoAuditTimeoutItems() {
        int count = wishListService.autoAuditTimeoutItems();
        return ResponseResult.success(count);
    }
}
