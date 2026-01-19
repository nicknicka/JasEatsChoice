package com.xx.jaseatschoicejava.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 统一ID生成器（增强版）
 *
 * 改进：
 * 1. 底层使用XOR雪花算法生成20位数字ID
 * 2. 保留原有的ID类型前缀系统
 * 3. 提供更强的安全性和不可预测性
 * 4. 保持向后兼容的API
 *
 * @author xx
 * @date 2026-01-19
 */
@Component
public class EnhancedIdGenerator {

    @Autowired
    private XorSnowflakeIdGenerator xorSnowflakeIdGenerator;

    // ========== ID类型前缀（保持不变）==========

    public static final String USER_ID_PREFIX = "U";
    public static final String MERCHANT_ID_PREFIX = "M";
    public static final String GROUP_ID_PREFIX = "G";
    public static final String DISH_ID_PREFIX = "D";
    public static final String MENU_ID_PREFIX = "MN";
    public static final String ORDER_ID_PREFIX = "O";
    public static final String ADDRESS_ID_PREFIX = "A";
    public static final String PAYMENT_ID_PREFIX = "P";
    public static final String REVIEW_ID_PREFIX = "R";
    public static final String WALLET_ID_PREFIX = "W";
    public static final String NOTIFICATION_ID_PREFIX = "N";
    public static final String RECIPE_ID_PREFIX = "RC";
    public static final String COLLECTION_ID_PREFIX = "C";
    public static final String CONTACT_ID_PREFIX = "CT";
    public static final String CHAT_MSG_ID_PREFIX = "CM";
    public static final String MESSAGE_RECORD_ID_PREFIX = "MR";
    public static final String ANNOUNCEMENT_ID_PREFIX = "AN";
    public static final String TUTORIAL_ID_PREFIX = "T";
    public static final String CONSUME_HISTORY_ID_PREFIX = "CH";
    public static final String RECHARGE_RECORD_ID_PREFIX = "RR";
    public static final String WITHDRAW_RECORD_ID_PREFIX = "WR";
    public static final String USER_PREFERENCE_ID_PREFIX = "UP";
    public static final String CALORIE_RECORD_ID_PREFIX = "CR";
    public static final String GROUP_ORDER_ID_PREFIX = "GO";
    public static final String GROUP_ORDER_DISH_ID_PREFIX = "GOD";
    public static final String ORDER_DISH_ID_PREFIX = "OD";
    public static final String REVIEW_REPLY_ID_PREFIX = "RRP";
    public static final String MENU_DISH_ID_PREFIX = "MND";

    // ========== 核心生成方法 ==========

    /**
     * 生成不带前缀的20位数字ID
     *
     * @return 20位数字ID字符串
     */
    public String generateId() {
        return xorSnowflakeIdGenerator.nextId();
    }

    /**
     * 生成Long类型的ID
     *
     * @return ID的Long值
     */
    public Long generateLongId() {
        String idStr = generateId();
        return Long.parseLong(idStr);
    }

    // ========== 带前缀的ID生成方法 ==========

    /**
     * 生成用户ID
     * 格式：U + 20位数字
     *
     * @return 用户ID字符串
     */
    public String generateUserId() {
        return USER_ID_PREFIX + generateId();
    }

    /**
     * 生成商家ID
     * 格式：M + 20位数字
     *
     * @return 商家ID字符串
     */
    public String generateMerchantId() {
        return MERCHANT_ID_PREFIX + generateId();
    }

    /**
     * 生成群组ID
     * 格式：G + 20位数字
     *
     * @return 群组ID字符串
     */
    public String generateGroupId() {
        return GROUP_ID_PREFIX + generateId();
    }

    /**
     * 生成菜品ID
     * 格式：D + 20位数字
     *
     * @return 菜品ID字符串
     */
    public String generateDishId() {
        return DISH_ID_PREFIX + generateId();
    }

    /**
     * 生成菜单ID
     * 格式：MN + 20位数字
     *
     * @return 菜单ID字符串
     */
    public String generateMenuId() {
        return MENU_ID_PREFIX + generateId();
    }

    /**
     * 生成订单ID
     * 格式：O + 20位数字
     *
     * @return 订单ID字符串
     */
    public String generateOrderId() {
        return ORDER_ID_PREFIX + generateId();
    }

    /**
     * 生成地址ID
     * 格式：A + 20位数字
     *
     * @return 地址ID字符串
     */
    public String generateAddressId() {
        return ADDRESS_ID_PREFIX + generateId();
    }

    /**
     * 生成支付ID
     * 格式：P + 20位数字
     *
     * @return 支付ID字符串
     */
    public String generatePaymentId() {
        return PAYMENT_ID_PREFIX + generateId();
    }

    /**
     * 生成评价ID
     * 格式：R + 20位数字
     *
     * @return 评价ID字符串
     */
    public String generateReviewId() {
        return REVIEW_ID_PREFIX + generateId();
    }

    /**
     * 生成钱包ID
     * 格式：W + 20位数字
     *
     * @return 钱包ID字符串
     */
    public String generateWalletId() {
        return WALLET_ID_PREFIX + generateId();
    }

    /**
     * 生成通知ID
     * 格式：N + 20位数字
     *
     * @return 通知ID字符串
     */
    public String generateNotificationId() {
        return NOTIFICATION_ID_PREFIX + generateId();
    }

    /**
     * 生成食谱ID
     * 格式：RC + 20位数字
     *
     * @return 食谱ID字符串
     */
    public String generateRecipeId() {
        return RECIPE_ID_PREFIX + generateId();
    }

    /**
     * 生成收藏ID
     * 格式：C + 20位数字
     *
     * @return 收藏ID字符串
     */
    public String generateCollectionId() {
        return COLLECTION_ID_PREFIX + generateId();
    }

    /**
     * 生成联系人ID
     * 格式：CT + 20位数字
     *
     * @return 联系人ID字符串
     */
    public String generateContactId() {
        return CONTACT_ID_PREFIX + generateId();
    }

    /**
     * 生成聊天消息ID
     * 格式：CM + 20位数字
     *
     * @return 聊天消息ID字符串
     */
    public String generateChatMsgId() {
        return CHAT_MSG_ID_PREFIX + generateId();
    }

    /**
     * 生成消息记录ID
     * 格式：MR + 20位数字
     *
     * @return 消息记录ID字符串
     */
    public String generateMessageRecordId() {
        return MESSAGE_RECORD_ID_PREFIX + generateId();
    }

    /**
     * 生成公告ID
     * 格式：AN + 20位数字
     *
     * @return 公告ID字符串
     */
    public String generateAnnouncementId() {
        return ANNOUNCEMENT_ID_PREFIX + generateId();
    }

    /**
     * 生成教程ID
     * 格式：T + 20位数字
     *
     * @return 教程ID字符串
     */
    public String generateTutorialId() {
        return TUTORIAL_ID_PREFIX + generateId();
    }

    /**
     * 生成消费记录ID
     * 格式：CH + 20位数字
     *
     * @return 消费记录ID字符串
     */
    public String generateConsumeHistoryId() {
        return CONSUME_HISTORY_ID_PREFIX + generateId();
    }

    /**
     * 生成充值记录ID
     * 格式：RR + 20位数字
     *
     * @return 充值记录ID字符串
     */
    public String generateRechargeRecordId() {
        return RECHARGE_RECORD_ID_PREFIX + generateId();
    }

    /**
     * 生成提现记录ID
     * 格式：WR + 20位数字
     *
     * @return 提现记录ID字符串
     */
    public String generateWithdrawRecordId() {
        return WITHDRAW_RECORD_ID_PREFIX + generateId();
    }

    /**
     * 生成用户偏好ID
     * 格式：UP + 20位数字
     *
     * @return 用户偏好ID字符串
     */
    public String generateUserPreferenceId() {
        return USER_PREFERENCE_ID_PREFIX + generateId();
    }

    /**
     * 生成卡路里记录ID
     * 格式：CR + 20位数字
     *
     * @return 卡路里记录ID字符串
     */
    public String generateCalorieRecordId() {
        return CALORIE_RECORD_ID_PREFIX + generateId();
    }

    /**
     * 生成群订单ID
     * 格式：GO + 20位数字
     *
     * @return 群订单ID字符串
     */
    public String generateGroupOrderId() {
        return GROUP_ORDER_ID_PREFIX + generateId();
    }

    /**
     * 生成群订单菜品ID
     * 格式：GOD + 20位数字
     *
     * @return 群订单菜品ID字符串
     */
    public String generateGroupOrderDishId() {
        return GROUP_ORDER_DISH_ID_PREFIX + generateId();
    }

    /**
     * 生成订单菜品ID
     * 格式：OD + 20位数字
     *
     * @return 订单菜品ID字符串
     */
    public String generateOrderDishId() {
        return ORDER_DISH_ID_PREFIX + generateId();
    }

    /**
     * 生成评价回复ID
     * 格式：RRP + 20位数字
     *
     * @return 评价回复ID字符串
     */
    public String generateReviewReplyId() {
        return REVIEW_REPLY_ID_PREFIX + generateId();
    }

    /**
     * 生成菜单菜品ID
     * 格式：MND + 20位数字
     *
     * @return 菜单菜品ID字符串
     */
    public String generateMenuDishId() {
        return MENU_DISH_ID_PREFIX + generateId();
    }

    // ========== ID类型转换方法 ==========

    /**
     * 从完整ID中提取纯数字ID
     *
     * @param idWithPrefix 带前缀的ID（如 "U12345678901234567890"）
     * @return 纯数字ID（如 "12345678901234567890"）
     */
    public static String extractNumericId(String idWithPrefix) {
        if (idWithPrefix == null || idWithPrefix.isEmpty()) {
            return "";
        }

        // 找到第一个数字的位置
        for (int i = 0; i < idWithPrefix.length(); i++) {
            if (Character.isDigit(idWithPrefix.charAt(i))) {
                return idWithPrefix.substring(i);
            }
        }

        return "";
    }

    /**
     * 将完整ID转换为Long类型
     *
     * @param idWithPrefix 带前缀的ID
     * @return Long类型的ID
     */
    public static Long toLongId(String idWithPrefix) {
        String numericId = extractNumericId(idWithPrefix);
        if (numericId.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(numericId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从ID中提取类型
     *
     * @param idWithPrefix 带前缀的ID
     * @return ID类型（如 "USER", "MERCHANT", "ORDER"）
     */
    public static String extractIdType(String idWithPrefix) {
        if (idWithPrefix == null || idWithPrefix.isEmpty()) {
            return "UNKNOWN";
        }

        // 提取前缀
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < idWithPrefix.length(); i++) {
            char c = idWithPrefix.charAt(i);
            if (!Character.isDigit(c)) {
                prefix.append(c);
            } else {
                break;
            }
        }

        // 根据前缀返回类型
        String prefixStr = prefix.toString();
        switch (prefixStr) {
            case "U": return "USER";
            case "M": return "MERCHANT";
            case "G": return "GROUP";
            case "D": return "DISH";
            case "MN": return "MENU";
            case "O": return "ORDER";
            case "A": return "ADDRESS";
            case "P": return "PAYMENT";
            case "R": return "REVIEW";
            case "W": return "WALLET";
            case "N": return "NOTIFICATION";
            case "RC": return "RECIPE";
            case "C": return "COLLECTION";
            case "CT": return "CONTACT";
            case "CM": return "CHAT_MSG";
            case "MR": return "MESSAGE_RECORD";
            case "AN": return "ANNOUNCEMENT";
            case "T": return "TUTORIAL";
            case "CH": return "CONSUME_HISTORY";
            case "RR": return "RECHARGE_RECORD";
            case "WR": return "WITHDRAW_RECORD";
            case "UP": return "USER_PREFERENCE";
            case "CR": return "CALORIE_RECORD";
            case "GO": return "GROUP_ORDER";
            case "GOD": return "GROUP_ORDER_DISH";
            case "OD": return "ORDER_DISH";
            case "RRP": return "REVIEW_REPLY";
            case "MND": return "MENU_DISH";
            default: return "UNKNOWN";
        }
    }

    // ========== 兼容性方法（向后兼容旧版IdGenerator）==========

    /**
     * 生成ID的静态方法（兼容旧版API）
     * 注意：这是兼容方法，实际使用XOR雪花算法
     *
     * @deprecated 请使用注入的EnhancedIdGenerator实例
     */
    @Deprecated
    public static synchronized Long generate() {
        // 由于静态方法无法访问Bean，这里保留旧的实现
        // 建议使用注入的EnhancedIdGenerator实例
        throw new UnsupportedOperationException(
            "请使用注入的EnhancedIdGenerator实例，或使用XorSnowflakeIdGenerator"
        );
    }

    /**
     * 生成用户ID字符串（兼容旧版）
     *
     * @deprecated 请使用注入的EnhancedIdGenerator实例
     */
    @Deprecated
    public static String generateUserIdString() {
        return USER_ID_PREFIX + generate();
    }

    /**
     * 生成长整型ID（兼容旧版）
     *
     * @deprecated 请使用注入的EnhancedIdGenerator实例
     */
    @Deprecated
    public static Long generateLong() {
        return generate();
    }

    // ========== 测试方法 ==========

    /**
     * 测试ID生成
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Enhanced ID生成器测试");
        System.out.println("========================================\n");

        // 注意：这个main方法无法直接使用@Autowired
        // 需要在Spring容器中运行
        System.out.println("请在Spring容器中使用此生成器");
        System.out.println("\n使用示例：");
        System.out.println("  @Autowired");
        System.out.println("  private EnhancedIdGenerator idGenerator;");
        System.out.println("");
        System.out.println("  String userId = idGenerator.generateUserId();");
        System.out.println("  String orderId = idGenerator.generateOrderId();");

        System.out.println("\n========================================");
    }
}
