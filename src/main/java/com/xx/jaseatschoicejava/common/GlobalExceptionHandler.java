package com.xx.jaseatschoicejava.common;

import com.xx.jaseatschoicejava.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 * 用于捕获和处理项目中所有控制器抛出的异常
 * 使用 @RestControllerAdvice 实现全局异常拦截，自动处理 JSON 响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     * 当项目中抛出 BusinessException 时，会自动调用此方法
     * @param e 业务异常对象
     * @return 标准化的响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseResult<?> handleBusinessException(BusinessException e) {
        // 记录业务异常日志（仅记录代码和消息，不记录堆栈轨迹）
        logger.error("Business Exception: Code={}, Message={}", e.getCode(), e.getMessage());
        // 返回标准化的失败响应
        return ResponseResult.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理所有其他未捕获的异常
     * 作为全局兜底异常处理，确保不会返回原始错误信息给客户端
     * @param e 异常对象
     * @return 标准化的响应结果
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<?> handleGlobalException(Exception e) {
        // 记录完整的异常信息和堆栈轨迹
        logger.error("Unexpected Exception:", e);
        // 返回标准化的系统异常响应
        return ResponseResult.fail("500", "系统异常，请联系管理员");
    }
}
