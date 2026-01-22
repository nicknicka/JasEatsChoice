package com.xx.jaseatschoicejava.common;

import com.xx.jaseatschoicejava.exception.BusinessException;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
     * 处理客户端断开连接异常
     * 当客户端在服务器完成响应之前断开连接时调用此方法
     * 这通常是因为用户刷新页面、关闭浏览器或取消请求导致的
     * @param e 客户端断开连接异常对象
     * @return null（因为连接已经断开，无需返回响应）
     */
    @ExceptionHandler(ClientAbortException.class)
    public ResponseResult<?> handleClientAbortException(ClientAbortException e) {
        // 仅在 DEBUG 级别记录日志，避免日志污染
        logger.debug("Client aborted the connection: {}", e.getMessage());
        // 连接已断开，无需返回响应
        return null;
    }

    /**
     * 处理方法参数类型不匹配异常
     * 当前端传递的参数类型与后端方法参数类型不匹配时调用此方法
     * 特别是处理将字符串"undefined"转换为Long类型失败的情况
     * @param e 参数类型不匹配异常对象
     * @return 标准化的响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseResult<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        // 记录异常日志
        logger.error("MethodArgumentTypeMismatchException: Parameter={}, ExpectedType={}, Value={}",
            e.getPropertyName(), e.getRequiredType().getSimpleName(), e.getValue());

        // 处理"undefined"字符串和"NaN"字符串的特殊情况
        String value = String.valueOf(e.getValue());
        if ("undefined".equals(value) || "NaN".equals(value)) {
            return ResponseResult.fail("400", "参数错误：ID不能为空或无效");
        }

        // 处理其他类型不匹配情况
        return ResponseResult.fail("400", "参数类型错误：" + e.getPropertyName() + " 应是 " + e.getRequiredType().getSimpleName() + " 类型");
    }

    /**
     * 处理数据完整性约束异常
     * 当数据库操作违反数据完整性约束时调用此方法
     * 如JSON格式错误、字段长度超限等
     * @param e 数据完整性约束异常对象
     * @return 标准化的响应结果
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseResult<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        // 记录异常日志
        logger.error("DataIntegrityViolationException: {}", e.getMessage());

        // 处理JSON格式错误的特殊情况
        if (e.getMessage() != null && (e.getMessage().contains("JSON") || e.getMessage().contains("json"))) {
            // Check for business_hours specific error
            if (e.getMessage().contains("business_hours")) {
                return ResponseResult.fail("400", "参数错误：营业时间格式无效，请确保为有效的JSON格式");
            }
            // Check for album specific error
            if (e.getMessage().contains("album")) {
                return ResponseResult.fail("400", "参数错误：店铺相册格式无效，请确保为有效的JSON格式");
            }
            // Generic JSON error
            return ResponseResult.fail("400", "参数错误：JSON格式无效，请确保为有效的JSON格式");
        }

        // 处理其他数据完整性约束错误
        return ResponseResult.fail("400", "参数错误：数据格式不符合要求");
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
