package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 购物车项实体。
 *
 * <p>同一用户在同一商家下，{@code dishId + spec} 唯一的组合只允许一行，
 * 由数据库唯一键兜底，避免并发加购堆出重复项。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_cart_item")
public class CartItem {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID（来自验签身份，不接受请求体传入）
     */
    private String userId;

    /**
     * 商家ID（加购时可缺省，由菜品反查补齐）
     */
    private String merchantId;

    /**
     * 菜品ID
     */
    private String dishId;

    /**
     * 规格，参与去重；无规格存空串
     */
    private String spec;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 可选食材，JSON 数组字符串
     */
    private String optionalIngredients;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否选中
     */
    private Boolean selected;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
