package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 绑定小红书登录态请求
 */
@Data
public class XhsSessionBindDTO {

    /**
     * 小红书登录态 Cookie
     */
    private String cookie;
}
