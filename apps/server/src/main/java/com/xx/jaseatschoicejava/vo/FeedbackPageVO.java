package com.xx.jaseatschoicejava.vo;

import lombok.Data;

import java.util.List;

/**
 * 反馈分页视图。用扁平的 list/total/page/size，避免客户端去猜 MyBatis-Plus 的 records 结构。
 *
 * <p>泛型是为了让管理端列表能放 {@link AdminFeedbackVO}，而用户端仍是 {@link FeedbackVO}；
 * 两者 JSON 形状一致，客户端无需区分。
 */
@Data
public class FeedbackPageVO<T> {

    private List<T> list;
    private long total;
    private long page;
    private long size;

    public FeedbackPageVO(List<T> list, long total, long page, long size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
