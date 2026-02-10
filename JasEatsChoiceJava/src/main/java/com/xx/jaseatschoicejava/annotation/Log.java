package com.xx.jaseatschoicejava.annotation;

import java.lang.annotation.*;

/**
 * 系统日志注解
 * 用于标记需要记录日志的方法
 *
 * 使用示例：
 * <pre>
 * @Log(module = "用户管理", operationType = "CREATE", description = "创建新用户")
 * public Result createUser(@RequestBody UserDTO userDTO) {
 *     // 业务逻辑
 * }
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作模块
     * 例如：用户管理、商家管理、订单管理等
     */
    String module() default "";

    /**
     * 操作类型
     * 例如：LOGIN、LOGOUT、CREATE、UPDATE、DELETE、QUERY、EXPORT、OTHER
     */
    String operationType() default "OTHER";

    /**
     * 操作描述
     * 支持 SpEL 表达式，例如：'#userDTO.name'
     */
    String description() default "";

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录返回结果
     */
    boolean recordResult() default false;

    /**
     * 是否记录执行时长
     */
    boolean recordTime() default true;

    /**
     * 敏感参数字段（需要脱敏）
     * 例如：{"password", "oldPassword", "newPassword", "phone", "idCard"}
     */
    String[] sensitiveFields() default {"password", "pwd"};

    /**
     * 是否异步记录日志
     */
    boolean async() default true;

    /**
     * 操作失败是否记录
     */
    boolean logOnFailure() default true;
}
