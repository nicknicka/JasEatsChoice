package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.WishListAppealDTO;
import com.xx.jaseatschoicejava.dto.WishListAuditDTO;
import com.xx.jaseatschoicejava.dto.WishListItemCreateDTO;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.WishListService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
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

 * @since 2025-01-30
 */
@RestController
@RequestMapping("/v1/wish-list")
public class WishListController {

    private static final Logger log = LoggerFactory.getLogger(WishListController.class);

    @Autowired
    private WishListService wishListService;

    @Autowired
    private UserService userService;

    /**
     * 取当前登录用户。
     * <p>
     * 身份只认 JwtAuthenticationFilter 验签后写入的可信用户ID；此前这里读的
     * {@code request.getAttribute("userId")} 从来没有写入点，取不到就回落成写死的
     * {@code test_user}，等于任何登录用户都能以同一个测试身份读写想吃列表（#593）。
     *
     * @param request HTTP请求
     * @return 用户实体；未登录或用户不存在时返回 null
     */
    private User resolveCurrentUser(HttpServletRequest request) {
        String userId = UserIdentityUtil.extractVerifiedUserId(request);
        if (userId == null) {
            return null;
        }
        return userService.getById(userId);
    }

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
        User currentUser = resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseResult.fail("401", "未登录或登录状态已失效");
        }

        String itemId = wishListService.createWishListItem(dto, currentUser.getUserId());
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
        User currentUser = resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseResult.fail("401", "未登录或登录状态已失效");
        }

        List<WishListItemDetailVO> list = wishListService.getUserWishList(currentUser.getUserId());
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
        User currentUser = resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseResult.fail("401", "未登录或登录状态已失效");
        }

        // 商家归属取自用户表的 merchant_id；未注册为商家的账号不能看待审核列表
        String merchantId = currentUser.getMerchantId();
        if (merchantId == null || merchantId.isBlank()) {
            return ResponseResult.fail("403", "当前账号未注册为商家");
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
        User currentUser = resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseResult.fail("401", "未登录或登录状态已失效");
        }

        String auditorId = currentUser.getUserId();
        String auditorName = currentUser.getNickname();
        if (auditorName == null || auditorName.isBlank()) {
            auditorName = "用户" + auditorId;
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
        User currentUser = resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseResult.fail("401", "未登录或登录状态已失效");
        }

        boolean success = wishListService.appealWishListItem(dto, currentUser.getUserId());
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
        User currentUser = resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseResult.fail("401", "未登录或登录状态已失效");
        }

        boolean success = wishListService.withdrawWishListItem(itemId, currentUser.getUserId());
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
