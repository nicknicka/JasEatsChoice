package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Data
@TableName("t_collection")
public class UserCollection {

    @TableId(type = IdType.ASSIGN_ID)
    private String id; // 主键ID

    private String userId; // 用户ID

    private String collectableType; // 收藏类型：merchant, dish, article, recipe等

    private String collectableId; // 收藏对象ID

    private LocalDateTime createTime; // 创建时间

    // 显式声明 getter 方法，确保编译器能识别
    public String getCollectableType() {
        return collectableType;
    }

    public String getCollectableId() {
        return collectableId;
    }
}
