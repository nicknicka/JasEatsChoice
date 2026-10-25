package com.xx.jaseatschoicejava.common;

import lombok.Data;

/**
 * 统一响应结果类
 */
@Data
public class ResponseResult<T> {

    private Boolean success;
    private String code;
    private String message;
    private T data;

    private ResponseResult() {
    }

    /**
     * 成功响应
     */
    public static <T> ResponseResult<T> success() {
        ResponseResult<T> result = new ResponseResult<>();
        result.setSuccess(true);
        result.setCode("200");
        result.setMessage("成功");
        return result;
    }

    /**
     * 成功响应并返回数据
     */
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setSuccess(true);
        result.setCode("200");
        result.setMessage("成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应并返回数据和自定义消息
     */
    public static <T> ResponseResult<T> success(T data, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setSuccess(true);
        result.setCode("200");
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败响应
     */
    public static <T> ResponseResult<T> fail(String code, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
