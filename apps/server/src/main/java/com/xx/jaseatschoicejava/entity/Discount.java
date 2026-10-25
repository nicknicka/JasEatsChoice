
package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 优惠活动实体类
 */
@Data
@TableName("discount")
public class Discount {
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "优惠ID")
    private String id;

    @ApiModelProperty(value = "商家ID")
    private String merchantId; // 商家ID

    @ApiModelProperty(value = "优惠名称")
    private String name;

    @ApiModelProperty(value = "优惠类型")
    private String type; // 满减、折扣、买赠、特价等

    @ApiModelProperty(value = "优惠力度")
    private Double discountValue; // 满减金额、折扣百分比、特价金额等

    @ApiModelProperty(value = "最低消费")
    private Double minAmount; // 满减活动的最低消费金额

    @ApiModelProperty(value = "每人限领")
    private Integer limitPerUser; // 每个用户最多可领取的数量

    @ApiModelProperty(value = "优惠描述")
    private String description;

    @ApiModelProperty(value = "使用说明")
    private String usageNotes; // 使用注意事项

    @ApiModelProperty(value = "优惠状态")
    private String status; // active: 启用, inactive: 禁用

    @ApiModelProperty(value = "有效期类型")
    private String validityType; // permanent: 永久有效, time_range: 时间段, days: 领取后天数

    @ApiModelProperty(value = "有效期范围")
    private String validityPeriod; // 时间段的开始和结束时间

    @ApiModelProperty(value = "有效天数")
    private Integer validDays; // 领取后的有效天数

    @ApiModelProperty(value = "使用次数")
    private Integer usedCount; // 已使用次数

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
