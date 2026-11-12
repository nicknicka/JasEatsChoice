package com.xx.jaseatschoicejava.vo;

import lombok.Data;

import java.util.List;

/**
 * 反馈分页视图。用扁平的 list/total/page/size，避免客户端去猜 MyBatis-Plus 的 records 结构。
 */
@Data
public class FeedbackPageVO {

    private List<FeedbackVO> list;
    private long total;
    private long page;
    private long size;

    public FeedbackPageVO(List<FeedbackVO> list, long total, long page, long size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
