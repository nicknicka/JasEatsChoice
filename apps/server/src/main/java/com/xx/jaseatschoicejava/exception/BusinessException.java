package com.xx.jaseatschoicejava.exception;

/**
 * 业务异常类
 * 用于处理项目中的业务逻辑异常
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误代码
     */
    private String code;

    /**
     * 构造方法
     * @param code 错误代码
     * @param message 错误信息
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
