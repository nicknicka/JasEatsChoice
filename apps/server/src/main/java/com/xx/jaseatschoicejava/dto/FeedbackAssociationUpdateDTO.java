package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 管理员修正反馈订单/商家关联的入参。
 */
@Data
public class FeedbackAssociationUpdateDTO {

    private String orderId;

    private String merchantId;
}
