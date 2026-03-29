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

    // 显式声明 getter/setter 方法，确保编译器能识别
    public String getCollectableType() {
        return collectableType;
    }

    public void setCollectableType(String collectableType) {
        this.collectableType = collectableType;
    }

    public String getCollectableId() {
        return collectableId;
    }

    public void setCollectableId(String collectableId) {
        this.collectableId = collectableId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
