package com.xx.jaseatschoicejava.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 敏感数据脱敏工具类
 * 用于对日志中的敏感信息进行脱敏处理
 */
public class SensitiveDataMasker {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveDataMasker.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 默认需要脱敏的字段名
    private static final Set<String> DEFAULT_SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "password", "pwd", "passwd",
            "oldPassword", "newPassword", "confirmPassword",
            "phone", "mobile", "telephone",
            "idCard", "idCardNo", "idcard",
            "bankCard", "bankAccount",
            "email", "emailAddress",
            "token", "accessToken", "refreshToken",
            "secret", "secretKey", "apiKey",
            "verifyCode", "captcha", "code"
    ));

    // 手机号正则
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 身份证号正则
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{15}|\\d{18}|\\d{17}[Xx]$");
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    // 银行卡号正则
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("^\\d{16,19}$");

    /**
     * 对JSON字符串进行脱敏处理
     *
     * @param jsonStr JSON字符串
     * @param sensitiveFields 需要额外脱敏的字段列表
     * @return 脱敏后的JSON字符串
     */
    public static String maskSensitiveData(String jsonStr, List<String> sensitiveFields) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return jsonStr;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(jsonStr);

            if (rootNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) rootNode;
                maskObjectNode(objectNode, sensitiveFields);
                return objectMapper.writeValueAsString(objectNode);
            } else if (rootNode.isArray()) {
                // 处理数组
                return maskArrayNode(rootNode, sensitiveFields);
            }

            return jsonStr;
        } catch (Exception e) {
            logger.warn("脱敏处理失败，返回原始字符串: {}", e.getMessage());
            return jsonStr;
        }
    }

    /**
     * 对对象节点进行脱敏
     */
    private static void maskObjectNode(ObjectNode objectNode, List<String> sensitiveFields) {
        objectNode.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode valueNode = entry.getValue();

            // 检查是否是需要脱敏的字段
            if (isSensitiveField(fieldName, sensitiveFields)) {
                String maskedValue = maskValue(fieldName, valueNode.asText());
                objectNode.put(fieldName, maskedValue);
            } else if (valueNode.isObject()) {
                // 递归处理嵌套对象
                maskObjectNode((ObjectNode) valueNode, sensitiveFields);
            } else if (valueNode.isArray()) {
                // 递归处理数组
                maskArrayInPlace(valueNode, sensitiveFields);
            }
        });
    }

    /**
     * 对数组节点进行脱敏
     */
    private static String maskArrayNode(JsonNode arrayNode, List<String> sensitiveFields) {
        try {
            maskArrayInPlace(arrayNode, sensitiveFields);
            return objectMapper.writeValueAsString(arrayNode);
        } catch (Exception e) {
            return arrayNode.toString();
        }
    }

    /**
     * 就地修改数组节点
     */
    private static void maskArrayInPlace(JsonNode arrayNode, List<String> sensitiveFields) {
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode element = arrayNode.get(i);
            if (element.isObject()) {
                maskObjectNode((ObjectNode) element, sensitiveFields);
            } else if (element.isArray()) {
                maskArrayInPlace(element, sensitiveFields);
            }
        }
    }

    /**
     * 判断是否是敏感字段
     */
    private static boolean isSensitiveField(String fieldName, List<String> additionalFields) {
        if (DEFAULT_SENSITIVE_FIELDS.contains(fieldName.toLowerCase())) {
            return true;
        }

        if (additionalFields != null) {
            return additionalFields.stream()
                    .anyMatch(field -> field.equalsIgnoreCase(fieldName));
        }

        return false;
    }

    /**
     * 对敏感值进行脱敏
     */
    private static String maskValue(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String lowerField = fieldName.toLowerCase();

        // 密码类字段：全部脱敏
        if (lowerField.contains("password") || lowerField.contains("pwd") || lowerField.contains("passwd")) {
            return "******";
        }

        // Token/Secret 类字段：只显示前4位和后4位
        if (lowerField.contains("token") || lowerField.contains("secret") || lowerField.contains("key")) {
            if (value.length() <= 8) {
                return "****";
            }
            return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
        }

        // 验证码类字段：全部脱敏
        if (lowerField.contains("code") || lowerField.contains("captcha")) {
            return "****";
        }

        // 手机号脱敏：保留前3位和后4位
        if (lowerField.contains("phone") || lowerField.contains("mobile") || PHONE_PATTERN.matcher(value).matches()) {
            return value.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }

        // 身份证号脱敏：保留前6位和后4位
        if (lowerField.contains("idcard") || ID_CARD_PATTERN.matcher(value).matches()) {
            int length = value.length();
            if (length == 15) {
                return value.substring(0, 6) + "*****" + value.substring(11);
            } else if (length == 18) {
                return value.substring(0, 6) + "********" + value.substring(14);
            }
            return "****";
        }

        // 邮箱脱敏：只显示第一个字符和@后面的域名
        if (lowerField.contains("email") || EMAIL_PATTERN.matcher(value).matches()) {
            int atIndex = value.indexOf('@');
            if (atIndex > 1) {
                return value.charAt(0) + "***" + value.substring(atIndex);
            }
            return "***" + value.substring(atIndex);
        }

        // 银行卡号脱敏：只显示前4位和后4位
        if (lowerField.contains("bank") || lowerField.contains("card") || BANK_CARD_PATTERN.matcher(value).matches()) {
            int length = value.length();
            if (length <= 8) {
                return "****";
            }
            return value.substring(0, 4) + "****" + value.substring(length - 4);
        }

        // 默认脱敏：保留前半部分
        if (value.length() <= 2) {
            return "**";
        } else if (value.length() <= 4) {
            return value.substring(0, 1) + "***";
        } else {
            return value.substring(0, value.length() / 2) + "****";
        }
    }

    /**
     * 脱敏手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 脱敏身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        int length = idCard.length();
        if (length == 15) {
            return idCard.substring(0, 6) + "*****" + idCard.substring(11);
        } else if (length == 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        return "****";
    }

    /**
     * 脱敏邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 1) {
            return email.charAt(0) + "***" + email.substring(atIndex);
        }
        return "***" + email.substring(atIndex);
    }

    /**
     * 脱敏银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.isEmpty()) {
            return bankCard;
        }
        int length = bankCard.length();
        if (length <= 8) {
            return "****";
        }
        return bankCard.substring(0, 4) + " **** " + bankCard.substring(length - 4);
    }
}
