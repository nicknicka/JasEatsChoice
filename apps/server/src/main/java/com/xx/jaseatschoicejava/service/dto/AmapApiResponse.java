package com.xx.jaseatschoicejava.service.dto;

/**
 * 高德接口统一响应包装。
 */
public record AmapApiResponse<T>(String code, String message, T data) {

    public boolean isSuccess() {
        return "200".equals(code);
    }
}