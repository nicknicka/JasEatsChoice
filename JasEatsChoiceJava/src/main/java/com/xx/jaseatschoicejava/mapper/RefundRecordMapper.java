package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.RefundRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 退款记录Mapper接口
 */
@Mapper
public interface RefundRecordMapper extends BaseMapper<RefundRecord> {

    /**
     * 分页查询退款记录列表（包含用户信息）
     */
    @Select("<script>" +
            "SELECT r.*, u.username, u.nickname " +
            "FROM t_refund_record r " +
            "LEFT JOIN t_user u ON r.user_id = u.user_id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (r.order_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR r.refund_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.username LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND r.status = #{status} " +
            "</if>" +
            "ORDER BY r.create_time DESC" +
            "</script>")
    IPage<RefundRecord> selectRefundPageWithUser(Page<RefundRecord> page,
                                                @Param("keyword") String keyword,
                                                @Param("status") String status);

    /**
     * 统计退款金额（按状态）
     */
    @Select("SELECT COALESCE(SUM(refund_amount), 0) FROM t_refund_record WHERE status = #{status}")
    java.math.BigDecimal sumRefundAmountByStatus(@Param("status") String status);
}
