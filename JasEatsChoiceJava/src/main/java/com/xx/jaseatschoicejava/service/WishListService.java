package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.AppealReplyDTO;
import com.xx.jaseatschoicejava.dto.WishListAppealDTO;
import com.xx.jaseatschoicejava.dto.WishListAuditDTO;
import com.xx.jaseatschoicejava.dto.WishListItemCreateDTO;
import com.xx.jaseatschoicejava.vo.WishListItemDetailVO;

import java.util.List;

/**
 * 想吃列表服务接口
 *
 * @author Claude
 * @since 2025-01-30
 */
public interface WishListService {

    /**
     * 创建想吃列表项
     *
     * @param dto 创建请求DTO
     * @param userId 用户ID
     * @return 列表项ID
     */
    String createWishListItem(WishListItemCreateDTO dto, String userId);

    /**
     * 用户查看自己的想吃列表
     *
     * @param userId 用户ID
     * @return 列表项详情列表
     */
    List<WishListItemDetailVO> getUserWishList(String userId);

    /**
     * 商家查看待审核列表
     *
     * @param merchantId 商家ID
     * @return 列表项详情列表
     */
    List<WishListItemDetailVO> getMerchantPendingList(String merchantId);

    /**
     * 审核想吃列表项
     *
     * @param dto 审核请求DTO
     * @param auditorId 审核人ID
     * @param auditorName 审核人姓名
     * @return 是否成功
     */
    boolean auditWishListItem(WishListAuditDTO dto, String auditorId, String auditorName);

    /**
     * 用户申诉
     *
     * @param dto 申诉请求DTO
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean appealWishListItem(WishListAppealDTO dto, String userId);

    /**
     * 商家回复申诉
     *
     * @param dto 申诉回复DTO
     * @param replierId 回复人ID
     * @return 是否成功
     */
    boolean replyAppeal(AppealReplyDTO dto, String replierId);

    /**
     * 用户撤回想吃列表项
     *
     * @param wishListItemId 列表项ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean withdrawWishListItem(String wishListItemId, String userId);

    /**
     * 获取列表项详情
     *
     * @param wishListItemId 列表项ID
     * @return 列表项详情
     */
    WishListItemDetailVO getWishListItemDetail(String wishListItemId);

    /**
     * 自动审核超时项目（定时任务）
     *
     * @return 自动通过的数量
     */
    int autoAuditTimeoutItems();
}
