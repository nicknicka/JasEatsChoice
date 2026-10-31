package com.xx.jaseatschoicejava.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 配置历史持久化实体。history_json 只含非密钥配置和 key 指纹。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_config_history")
public class AiConfigHistoryEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("history_json")
    private String historyJson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
