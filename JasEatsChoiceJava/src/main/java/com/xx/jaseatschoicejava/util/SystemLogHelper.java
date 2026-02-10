package com.xx.jaseatschoicejava.util;

import com.xx.jaseatschoicejava.entity.SystemLog;
import com.xx.jaseatschoicejava.service.SystemLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 系统日志辅助类
 * 提供便捷的日志记录方法
 *
 * 使用示例：
 * <pre>
 * // 在 Controller 或 Service 中使用
 * SystemLogHelper.logOperation(
 *     "用户管理",
 *     "CREATE",
 *     "创建用户：" + userDTO.getName(),
 *     adminId,
 *     adminName,
 *     "ADMIN",
 *     "UserController.createUser",
 *     userDTO,
 *     null
 * );
 * </pre>
 */
public class SystemLogHelper {

    private static final Logger logger = LoggerFactory.getLogger(SystemLogHelper.class);
    private static SystemLogService systemLogService;

    /**
     * 设置系统日志服务（由 Spring 自动注入）
     */
    public static void setSystemLogService(SystemLogService service) {
        systemLogService = service;
    }

    /**
     * 记录操作日志（带参数自动脱敏）
     *
     * @param module 操作模块
     * @param operationType 操作类型
     * @param description 操作描述
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @param operatorType 操作人类型
     * @param method 方法名
     * @param params 请求参数对象
     * @param result 返回结果对象
     */
    public static void logOperation(
            String module,
            String operationType,
            String description,
            Long operatorId,
            String operatorName,
            String operatorType,
            String method,
            Object params,
            Object result) {

        logOperation(module, operationType, description, operatorId, operatorName,
                operatorType, method, params, result, 0L, "SUCCESS", null);
    }

    /**
     * 记录操作日志（完整参数）
     */
    public static void logOperation(
            String module,
            String operationType,
            String description,
            Long operatorId,
            String operatorName,
            String operatorType,
            String method,
            Object params,
            Object result,
            Long executeTime,
            String status,
            String errorMessage) {

        try {
            if (systemLogService == null) {
                logger.warn("SystemLogService 未注入，无法记录日志");
                return;
            }

            SystemLog systemLog = new SystemLog();

            // 基本信息
            systemLog.setLogId(generateTraceId());
            systemLog.setModule(module);
            systemLog.setOperationType(operationType);
            systemLog.setDescription(truncateString(description, 500));

            // 操作人信息
            systemLog.setOperatorId(operatorId);
            systemLog.setOperatorName(operatorName);
            systemLog.setOperatorType(operatorType);

            // 方法信息
            systemLog.setMethod(method);

            // 参数和结果（自动脱敏）
            if (params != null) {
                String paramsJson = toJson(params);
                paramsJson = SensitiveDataMasker.maskSensitiveData(paramsJson, getDefaultSensitiveFields());
                systemLog.setParams(truncateString(paramsJson, 2000));
            }

            if (result != null) {
                String resultJson = toJson(result);
                resultJson = SensitiveDataMasker.maskSensitiveData(resultJson, getDefaultSensitiveFields());
                systemLog.setResult(truncateString(resultJson, 2000));
            }

            // 执行信息
            systemLog.setExecuteTime(executeTime);
            systemLog.setStatus(status);
            systemLog.setErrorMessage(truncateString(errorMessage, 1000));

            // 请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                systemLog.setIp(getClientIP(request));
                systemLog.setBrowser(getBrowser(request));
                systemLog.setOs(getOS(request));
            }

            // 时间戳
            systemLog.setCreateTime(LocalDateTime.now());

            // 异步保存
            saveLogAsync(systemLog);

        } catch (Exception e) {
            logger.error("记录系统日志失败", e);
        }
    }

    /**
     * 记录登录日志
     */
    public static void logLogin(Long adminId, String adminName, String ip, String status) {
        logOperation(
                "系统管理",
                "LOGIN",
                "管理员登录",
                adminId,
                adminName,
                "ADMIN",
                "AdminController.login",
                null,
                null,
                0L,
                status,
                null
        );
    }

    /**
     * 记录登出日志
     */
    public static void logLogout(Long adminId, String adminName) {
        logOperation(
                "系统管理",
                "LOGOUT",
                "管理员登出",
                adminId,
                adminName,
                "ADMIN",
                "AdminController.logout",
                null,
                null,
                0L,
                "SUCCESS",
                null
        );
    }

    /**
     * 记录创建操作日志
     */
    public static void logCreate(String module, String description, Long adminId, String adminName, Object params) {
        logOperation(
                module,
                "CREATE",
                description,
                adminId,
                adminName,
                "ADMIN",
                getCurrentMethodName(),
                params,
                null,
                0L,
                "SUCCESS",
                null
        );
    }

    /**
     * 记录更新操作日志
     */
    public static void logUpdate(String module, String description, Long adminId, String adminName, Object params) {
        logOperation(
                module,
                "UPDATE",
                description,
                adminId,
                adminName,
                "ADMIN",
                getCurrentMethodName(),
                params,
                null,
                0L,
                "SUCCESS",
                null
        );
    }

    /**
     * 记录删除操作日志
     */
    public static void logDelete(String module, String description, Long adminId, String adminName, Object params) {
        logOperation(
                module,
                "DELETE",
                description,
                adminId,
                adminName,
                "ADMIN",
                getCurrentMethodName(),
                params,
                null,
                0L,
                "SUCCESS",
                null
        );
    }

    /**
     * 记录异常日志
     */
    public static void logError(String module, String description, Long adminId, String adminName, Throwable e) {
        logOperation(
                module,
                "OTHER",
                description,
                adminId,
                adminName,
                "ADMIN",
                getCurrentMethodName(),
                null,
                null,
                0L,
                "FAILED",
                e.getMessage()
        );
    }

    /**
     * 异步保存日志
     */
    private static void saveLogAsync(SystemLog systemLog) {
        // 使用新线程异步保存
        new Thread(() -> {
            try {
                systemLogService.saveLog(systemLog);
            } catch (Exception e) {
                logger.error("异步保存系统日志失败", e);
            }
        }).start();
    }

    /**
     * 获取当前方法名
     */
    private static String getCurrentMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 跳过当前方法和 logOperation 方法
        if (stackTrace.length > 3) {
            return stackTrace[3].getClassName() + "." + stackTrace[3].getMethodName();
        }
        return "Unknown";
    }

    /**
     * 生成追踪ID
     */
    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 对象转JSON字符串
     */
    private static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    /**
     * 截断字符串
     */
    private static String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 获取客户端IP地址
     */
    private static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取浏览器类型
     */
    private static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Chrome")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari")) {
            return "Safari";
        } else if (userAgent.contains("Edge")) {
            return "Edge";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE";
        }
        return "Other";
    }

    /**
     * 获取操作系统
     */
    private static String getOS(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Mac")) {
            return "MacOS";
        } else if (userAgent.contains("Linux")) {
            return "Linux";
        } else if (userAgent.contains("Android")) {
            return "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        return "Other";
    }

    /**
     * 获取默认敏感字段列表
     */
    private static List<String> getDefaultSensitiveFields() {
        return Arrays.asList("password", "pwd", "oldPassword", "newPassword", "confirmPassword",
                "phone", "mobile", "idCard", "bankCard", "email", "token", "secret", "code");
    }
}
