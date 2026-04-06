package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模拟第三方用户（毕设演示用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_mock_oauth_user")
public class MockOAuthUser {

    @TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.INPUT)
    private String id;

    @TableField("provider")
    private String provider;

    @TableField("open_id")
    private String openId;

    @TableField("union_id")
    private String unionId;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("password")
    private String password;

    @TableField("create_time")
    private LocalDateTime createTime;
}
