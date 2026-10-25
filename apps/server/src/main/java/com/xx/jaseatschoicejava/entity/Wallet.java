package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_wallet")
@ApiModel(description = "用户钱包实体")
public class Wallet {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "钱包ID")
    private String id; // 钱包ID

    @ApiModelProperty(value = "用户ID")
    private String userId; // 用户ID

    @ApiModelProperty(value = "当前余额（元）")
    private BigDecimal balance; // 当前余额

    @ApiModelProperty(value = "累计充值金额")
    private BigDecimal totalRecharge; // 累计充值金额

    @ApiModelProperty(value = "累计消费金额")
    private BigDecimal totalConsume; // 累计消费金额

    @ApiModelProperty(value = "累计提现金额")
    private BigDecimal totalWithdraw; // 累计提现金额

    @Version
    @ApiModelProperty(value = "乐观锁版本号")
    private Integer version; // 乐观锁版本号

    @ApiModelProperty(value = "钱包状态：active-正常, frozen-冻结")
    private String status; // 钱包状态

    @ApiModelProperty(value = "是否锁定（用于钱包安全设置）")
    private Boolean locked; // 是否锁定

    @ApiModelProperty(value = "是否开启支付验证")
    private Boolean verifyEnabled; // 是否开启大额支付验证

    @ApiModelProperty(value = "单日交易限额")
    private BigDecimal dailyLimit; // 单日交易限额

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
