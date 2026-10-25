package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群成员关系实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_group_member")
@ApiModel(description = "群成员关系实体")
public class GroupMember {

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("group_id")
    @ApiModelProperty(value = "群ID")
    private String groupId;

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID")
    private String userId;

    @TableField("role")
    @ApiModelProperty(value = "角色：owner-群主，admin-管理员，member-普通成员")
    private String role;

    @TableField("join_time")
    @ApiModelProperty(value = "加入时间")
    private LocalDateTime joinTime;
}
