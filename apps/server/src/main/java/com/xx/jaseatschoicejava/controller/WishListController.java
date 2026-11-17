package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.WishListAppealDTO;
import com.xx.jaseatschoicejava.dto.WishListAuditDTO;
import com.xx.jaseatschoicejava.dto.WishListItemCreateDTO;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.security.CurrentIdentityResolver;
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

 * @since 2025-01-30
 */
@RestController
@RequestMapping("/v1/wish-list")
public class WishListController {

    private static final Logger log = LoggerFactory.getLogger(WishListController.class);

    @Autowired
    private WishListService wishListService;

    /**
     * 身份与归属判据统一取自共享组件（#597），本控制器不再自留一份实现。
     * <p>
     * 此前这里有个私有 {@code resolveCurrentUser}，重复了组件里
     * {@link CurrentIdentityResolver#currentUserOrNull} 的逻辑（#593 期间临时加的）；
     * 判据分散在控制器里必然再次漂移，故收敛掉。
     */
    @Autowired
    private CurrentIdentityResolver identityResolver;

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
        User currentUser = identityResolver.currentUserOrNull(request);
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
        User currentUser = identityResolver.currentUserOrNull(request);
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
        User currentUser = identityResolver.currentUserOrNull(request);
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
        User currentUser = identityResolver.currentUserOrNull(request);
        if (currentUser == null) {
            return ResponseResult.fail("401", "未登录或登录状态已失效");
        }

        String auditorId = currentUser.getUserId();
        String auditorName = currentUser.getNickname();
        if (auditorName == null || auditorName.isBlank()) {
            auditorName = "用户" + auditorId;
        }

        // 归属校验（#597）：审核的是"挂在某个商家名下"的想吃需求。
        // 此前只按 wishListItemId 定位，service 也不比对 merchant_id，
        // 因此任何登录用户都能受理/拒绝任意商家的需求。
        String wishListItemId = dto.getWishListItemId();
        if (wishListItemId == null || wishListItemId.isBlank()) {
            return ResponseResult.fail("400", "wishListItemId 不能为空");
        }
        WishListItemDetailVO target;
        try {
            target = wishListService.getWishListItemDetail(wishListItemId);
        } catch (RuntimeException e) {
            log.warn("审核时取想吃列表项失败：wishListItemId={}, reason={}", wishListItemId, e.getMessage());
            return ResponseResult.fail("404", "想吃列表项不存在");
        }
        if (target == null) {
            return ResponseResult.fail("404", "想吃列表项不存在");
        }
        if (!identityResolver.canManageMerchant(request, target.getMerchantId())) {
            return ResponseResult.fail("403", "无权审核该想吃需求");
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
        User currentUser = identityResolver.currentUserOrNull(request);
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
        User currentUser = identityResolver.currentUserOrNull(request);
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
        WishListItemDetailVO detail;
        try {
            detail = wishListService.getWishListItemDetail(itemId);
        } catch (RuntimeException e) {
            // service 查不到时抛 RuntimeException("想吃列表项不存在")，此前会冒泡成 500；
            // 这里按"不存在"处理。其余异常也一并记日志，避免把真实故障静默吞成 404。
            log.warn("查询想吃列表项详情失败：itemId={}", itemId, e);
            return ResponseResult.fail("404", "想吃列表项不存在");
        }

        if (detail == null) {
            return ResponseResult.fail("404", "想吃列表项不存在");
        }

        // #597：归属判据用 canAccessOwnedResource——列表项的归属方可能是用户ID，
        // 也可能是商家ID（商家端代客记录），两者任一匹配即可查看。
        // 不属于调用方时统一返回 404，不用 403，避免用主键探测他人列表项是否存在。
        if (!identityResolver.canAccessOwnedResource(request, detail.getUserId(), detail.getMerchantId())) {
            return ResponseResult.fail("404", "想吃列表项不存在");
        }

        return ResponseResult.success(detail);
    }

    /**
     * 自动审核超时项（定时任务调用）
     *
     * @return 处理数量
     */
    @PostMapping("/auto-audit-timeout")
    public ResponseResult<Integer> autoAuditTimeoutItems(HttpServletRequest request) {
        // #597：这是批处理触发器，定时任务直接调用 service，不经过本端点；
        // 对外只允许管理员手动触发，避免任何登录用户随意跑批量审核。
        if (!identityResolver.isAdmin()) {
            return ResponseResult.fail("403", "仅管理员可触发超时审核");
        }
        int count = wishListService.autoAuditTimeoutItems();
        return ResponseResult.success(count);
    }
}
