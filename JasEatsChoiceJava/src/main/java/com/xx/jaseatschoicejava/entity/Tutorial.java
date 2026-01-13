package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tutorial")
public class Tutorial {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String title;

    private String type; // video or article

    private String duration;

    private String views;

    private boolean featured; // 是否在首页推荐

    private String content;

    private String coverImage;

    private String videoUrl;

    private String author;

    private java.util.Date createTime;

    private java.util.Date updateTime;
}