package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.WishListItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 想吃列表Mapper接口
 *
 * @author Claude
 * @since 2025-01-30
 */
@Mapper
public interface WishListItemMapper extends BaseMapper<WishListItem> {

    /**
     * 查询用户的想吃列表
     *
     * @param userId 用户ID
     * @return 列表项列表
     */
    @Select("SELECT * FROM t_wish_list_item WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<WishListItem> selectByUserId(@Param("userId") String userId);

    /**
     * 查询商家待审核列表
     *
     * @param merchantId 商家ID
     * @return 列表项列表
     */
    @Select("SELECT * FROM t_wish_list_item WHERE merchant_id = #{merchantId} AND audit_status = 0 ORDER BY create_time ASC")
    List<WishListItem> selectPendingByMerchantId(@Param("merchantId") String merchantId);

    /**
     * 查询超时未审核的列表项
     *
     * @param now 当前时间
     * @return 列表项列表
     */
    @Select("SELECT * FROM t_wish_list_item WHERE audit_status = 0 AND timeout_time < #{now}")
    List<WishListItem> selectTimeoutItems(@Param("now") java.time.LocalDateTime now);
}
