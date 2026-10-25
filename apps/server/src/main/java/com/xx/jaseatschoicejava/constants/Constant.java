package com.xx.jaseatschoicejava.constants;

/**
 * 系统常量类
 *
 * @Author nickxiao
 * @Date 2025/11/22 13:53
 */
public class Constant {

    // -------------------------- 全局常量 --------------------------
    /**
     * 成功状态码
     */
    public static final String SUCCESS_CODE = "200";

    /**
     * 失败状态码
     */
    public static final String FAIL_CODE = "500";

    // -------------------------- JWT相关常量 --------------------------
    /**
     * JWT令牌头名称
     */
    public static final String JWT_HEADER = "Authorization";

    /**
     * JWT令牌前缀
     */
    public static final String JWT_PREFIX = "Bearer ";

    // -------------------------- WebSocket相关常量 --------------------------
    /**
     * WebSocket请求参数 - token
     */
    public static final String WS_PARAM_TOKEN = "token";

    // -------------------------- 数据库相关常量 --------------------------
    /**
     * 已读状态 - 已读
     */
    public static final Boolean READ_STATUS_READ = true;

    /**
     * 已读状态 - 未读
     */
    public static final Boolean READ_STATUS_UNREAD = false;

    // -------------------------- 日期时间相关常量 --------------------------
    /**
     * 标准日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 标准时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 标准日期时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}


