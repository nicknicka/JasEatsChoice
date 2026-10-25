package com.xx.jaseatschoicejava.entity;

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
 * 第三方 OAuth 账号关联实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_oauth")
@ApiModel(description = "第三方OAuth账号关联")
public class UserOAuth {

    @TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.INPUT)
    @ApiModelProperty(value = "主键ID (OA + 16位数字)")
    private String id;

    @TableField("user_id")
    @ApiModelProperty(value = "关联的本地用户ID")
    private String userId;

    @TableField("provider")
    @ApiModelProperty(value = "平台标识: wechat/qq")
    private String provider;

    @TableField("open_id")
    @ApiModelProperty(value = "第三方平台OpenID")
    private String openId;

    @TableField("union_id")
    @ApiModelProperty(value = "微信UnionID（打通小程序）")
    private String unionId;

    @TableField("nickname")
    @ApiModelProperty(value = "第三方平台昵称")
    private String nickname;

    @TableField("avatar_url")
    @ApiModelProperty(value = "第三方平台头像URL")
    private String avatarUrl;

    @TableField("access_token")
    @ApiModelProperty(value = "access_token")
    private String accessToken;

    @TableField("refresh_token")
    @ApiModelProperty(value = "refresh_token")
    private String refreshToken;

    @TableField("token_expires_at")
    @ApiModelProperty(value = "access_token过期时间")
    private LocalDateTime tokenExpiresAt;

    @TableField("create_time")
    @ApiModelProperty(value = "绑定时间")
    private LocalDateTime createTime;

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
