package com.xx.jaseatschoicejava.ai.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI工具函数处理方法注解
 * 用于标记处理特定AI函数的方法
 *
 * 使用示例：
 * <pre>
 * &#64;AiFunctionHandler("search_dishes")
 * public String handleSearchDishes(Map&lt;String, Object&gt; arguments) {
 *     // 实现逻辑
 * }
 * </pre>
 *
 * @author Claude
 * @since 2026-03-14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiFunctionHandler {

    /**
     * 函数名称，必须与 AiFunctionType 中的 functionName 一致
     */
    String value();

    /**
     * 函数描述（可选，用于日志和文档）
     */
    String description() default "";
}
