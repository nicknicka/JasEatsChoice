package com.xx.jaseatschoicejava.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 统一ID生成器
 * 1. 生成16位数字的ID
 * 2. 使用SHA-256哈希算法结合时间戳和随机数
 * 3. 生成后进行随机打乱以确保无序性
 * 4. 提供ID类型转换功能
 */
public class IdGenerator {

    // 随机数生成器
    private static final SecureRandom RANDOM = new SecureRandom();

    // ID类型前缀
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

    // ID长度
    public static final int ID_LENGTH = 16;

    // 哈希算法
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * 生成16位数字的ID
     * 流程：
     * 1. 组合当前时间戳和随机数
     * 2. 使用SHA-256哈希算法生成哈希值
     * 3. 从哈希值中提取数字部分
     * 4. 截取16位数字
     * 5. 对16位数字进行随机打乱
     * 6. 返回最终的16位数字ID
     */
    public static synchronized Long generateId() {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

            // 1. 生成组合字符串：时间戳 + 随机数
            String input = System.currentTimeMillis() + "-" + RANDOM.nextLong();

            // 2. 计算哈希值
            byte[] hashBytes = digest.digest(input.getBytes());

            // 3. 从哈希值中提取数字部分
            StringBuilder digitsBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                // 将字节转换为无符号整数并取后两位
                String byteStr = String.format("%02X", b & 0xFF);
                for (char c : byteStr.toCharArray()) {
                    if (Character.isDigit(c)) {
                        digitsBuilder.append(c);
                        if (digitsBuilder.length() >= ID_LENGTH * 2) { // 获取足够多的数字
                            break;
                        }
                    }
                }
                if (digitsBuilder.length() >= ID_LENGTH * 2) {
                    break;
                }
            }

            // 4. 确保至少有16位数字，如果不够则继续循环
            while (digitsBuilder.length() < ID_LENGTH) {
                // 生成更多数字
                input = System.currentTimeMillis() + "-" + RANDOM.nextLong() + "-" + RANDOM.nextLong();
                hashBytes = digest.digest(input.getBytes());

                for (byte b : hashBytes) {
                    String byteStr = String.format("%02X", b & 0xFF);
                    for (char c : byteStr.toCharArray()) {
                        if (Character.isDigit(c)) {
                            digitsBuilder.append(c);
                            if (digitsBuilder.length() >= ID_LENGTH) {
                                break;
                            }
                        }
                    }
                    if (digitsBuilder.length() >= ID_LENGTH) {
                        break;
                    }
                }
            }

            // 截取16位数字
            String digits = digitsBuilder.substring(0, ID_LENGTH);

            // 5. 随机打乱字符串
            String shuffled = shuffleString(digits);

            // 6. 转换为Long并返回
            return Long.parseLong(shuffled);

        } catch (NoSuchAlgorithmException e) {
            // 如果SHA-256不可用，回退到基于时间戳的方式
            e.printStackTrace();
            // 回退实现，确保系统可用性
            long timestamp = System.currentTimeMillis();
            long random = RANDOM.nextLong() % 10000000000000000L;
            return Math.abs(timestamp * 10000000000L + random % 10000000000L);
        }
    }

    /**
     * 随机打乱字符串
     * @param str 要打乱的字符串
     * @return 打乱后的字符串
     */
    private static String shuffleString(String str) {
        char[] chars = str.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            // 交换字符
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * 将用户ID转换为带U前缀的字符串
     * @param userId 用户ID
     * @return 带U前缀的用户ID字符串
     */
    public static String toUserIdString(Long userId) {
        if (userId == null) {
            return null;
        }
        return USER_ID_PREFIX + userId;
    }

    /**
     * 将商家ID转换为带M前缀的字符串
     * @param merchantId 商家ID
     * @return 带M前缀的商家ID字符串
     */
    public static String toMerchantIdString(Long merchantId) {
        if (merchantId == null) {
            return null;
        }
        return MERCHANT_ID_PREFIX + merchantId;
    }

    /**
     * 将群ID转换为带G前缀的字符串
     * @param groupId 群ID
     * @return 带G前缀的群ID字符串
     */
    public static String toGroupIdString(Long groupId) {
        if (groupId == null) {
            return null;
        }
        return GROUP_ID_PREFIX + groupId;
    }

    /**
     * 将菜品ID转换为带D前缀的字符串
     * @param dishId 菜品ID
     * @return 带D前缀的菜品ID字符串
     */
    public static String toDishIdString(Long dishId) {
        if (dishId == null) {
            return null;
        }
        return DISH_ID_PREFIX + dishId;
    }

    /**
     * 将菜单ID转换为带MN前缀的字符串
     * @param menuId 菜单ID
     * @return 带MN前缀的菜单ID字符串
     */
    public static String toMenuIdString(Long menuId) {
        if (menuId == null) {
            return null;
        }
        return MENU_ID_PREFIX + menuId;
    }

    /**
     * 将订单ID转换为带O前缀的字符串
     * @param orderId 订单ID
     * @return 带O前缀的订单ID字符串
     */
    public static String toOrderIdString(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return ORDER_ID_PREFIX + orderId;
    }

    /**
     * 将地址ID转换为带A前缀的字符串
     * @param addressId 地址ID
     * @return 带A前缀的地址ID字符串
     */
    public static String toAddressIdString(Long addressId) {
        if (addressId == null) {
            return null;
        }
        return ADDRESS_ID_PREFIX + addressId;
    }

    /**
     * 将支付记录ID转换为带P前缀的字符串
     * @param paymentId 支付记录ID
     * @return 带P前缀的支付记录ID字符串
     */
    public static String toPaymentIdString(Long paymentId) {
        if (paymentId == null) {
            return null;
        }
        return PAYMENT_ID_PREFIX + paymentId;
    }

    /**
     * 将评价ID转换为带R前缀的字符串
     * @param reviewId 评价ID
     * @return 带R前缀的评价ID字符串
     */
    public static String toReviewIdString(Long reviewId) {
        if (reviewId == null) {
            return null;
        }
        return REVIEW_ID_PREFIX + reviewId;
    }

    /**
     * 将钱包ID转换为带W前缀的字符串
     * @param walletId 钱包ID
     * @return 带W前缀的钱包ID字符串
     */
    public static String toWalletIdString(Long walletId) {
        if (walletId == null) {
            return null;
        }
        return WALLET_ID_PREFIX + walletId;
    }

    /**
     * 将通知ID转换为带N前缀的字符串
     * @param notificationId 通知ID
     * @return 带N前缀的通知ID字符串
     */
    public static String toNotificationIdString(Long notificationId) {
        if (notificationId == null) {
            return null;
        }
        return NOTIFICATION_ID_PREFIX + notificationId;
    }

    /**
     * 将食谱ID转换为带RC前缀的字符串
     * @param recipeId 食谱ID
     * @return 带RC前缀的食谱ID字符串
     */
    public static String toRecipeIdString(Long recipeId) {
        if (recipeId == null) {
            return null;
        }
        return RECIPE_ID_PREFIX + recipeId;
    }

    /**
     * 将收藏ID转换为带C前缀的字符串
     * @param collectionId 收藏ID
     * @return 带C前缀的收藏ID字符串
     */
    public static String toCollectionIdString(Long collectionId) {
        if (collectionId == null) {
            return null;
        }
        return COLLECTION_ID_PREFIX + collectionId;
    }

    /**
     * 将联系人ID转换为带CT前缀的字符串
     * @param contactId 联系人ID
     * @return 带CT前缀的联系人ID字符串
     */
    public static String toContactIdString(Long contactId) {
        if (contactId == null) {
            return null;
        }
        return CONTACT_ID_PREFIX + contactId;
    }

    /**
     * 将聊天消息ID转换为带CM前缀的字符串
     * @param chatMsgId 聊天消息ID
     * @return 带CM前缀的聊天消息ID字符串
     */
    public static String toChatMsgIdString(Long chatMsgId) {
        if (chatMsgId == null) {
            return null;
        }
        return CHAT_MSG_ID_PREFIX + chatMsgId;
    }

    /**
     * 将消息记录ID转换为带MR前缀的字符串
     * @param messageRecordId 消息记录ID
     * @return 带MR前缀的消息记录ID字符串
     */
    public static String toMessageRecordIdString(Long messageRecordId) {
        if (messageRecordId == null) {
            return null;
        }
        return MESSAGE_RECORD_ID_PREFIX + messageRecordId;
    }

    /**
     * 将公告ID转换为带AN前缀的字符串
     * @param announcementId 公告ID
     * @return 带AN前缀的公告ID字符串
     */
    public static String toAnnouncementIdString(Long announcementId) {
        if (announcementId == null) {
            return null;
        }
        return ANNOUNCEMENT_ID_PREFIX + announcementId;
    }

    /**
     * 将教程ID转换为带T前缀的字符串
     * @param tutorialId 教程ID
     * @return 带T前缀的教程ID字符串
     */
    public static String toTutorialIdString(Long tutorialId) {
        if (tutorialId == null) {
            return null;
        }
        return TUTORIAL_ID_PREFIX + tutorialId;
    }

    /**
     * 将消费记录ID转换为带CH前缀的字符串
     * @param consumeHistoryId 消费记录ID
     * @return 带CH前缀的消费记录ID字符串
     */
    public static String toConsumeHistoryIdString(Long consumeHistoryId) {
        if (consumeHistoryId == null) {
            return null;
        }
        return CONSUME_HISTORY_ID_PREFIX + consumeHistoryId;
    }

    /**
     * 将充值记录ID转换为带RR前缀的字符串
     * @param rechargeRecordId 充值记录ID
     * @return 带RR前缀的充值记录ID字符串
     */
    public static String toRechargeRecordIdString(Long rechargeRecordId) {
        if (rechargeRecordId == null) {
            return null;
        }
        return RECHARGE_RECORD_ID_PREFIX + rechargeRecordId;
    }

    /**
     * 将提现记录ID转换为带WR前缀的字符串
     * @param withdrawRecordId 提现记录ID
     * @return 带WR前缀的提现记录ID字符串
     */
    public static String toWithdrawRecordIdString(Long withdrawRecordId) {
        if (withdrawRecordId == null) {
            return null;
        }
        return WITHDRAW_RECORD_ID_PREFIX + withdrawRecordId;
    }

    /**
     * 将用户偏好ID转换为带UP前缀的字符串
     * @param userPreferenceId 用户偏好ID
     * @return 带UP前缀的用户偏好ID字符串
     */
    public static String toUserPreferenceIdString(Long userPreferenceId) {
        if (userPreferenceId == null) {
            return null;
        }
        return USER_PREFERENCE_ID_PREFIX + userPreferenceId;
    }

    /**
     * 将卡路里记录ID转换为带CR前缀的字符串
     * @param calorieRecordId 卡路里记录ID
     * @return 带CR前缀的卡路里记录ID字符串
     */
    public static String toCalorieRecordIdString(Long calorieRecordId) {
        if (calorieRecordId == null) {
            return null;
        }
        return CALORIE_RECORD_ID_PREFIX + calorieRecordId;
    }

    /**
     * 将群订单ID转换为带GO前缀的字符串
     * @param groupOrderId 群订单ID
     * @return 带GO前缀的群订单ID字符串
     */
    public static String toGroupOrderIdString(Long groupOrderId) {
        if (groupOrderId == null) {
            return null;
        }
        return GROUP_ORDER_ID_PREFIX + groupOrderId;
    }

    /**
     * 将群订单菜品ID转换为带GOD前缀的字符串
     * @param groupOrderDishId 群订单菜品ID
     * @return 带GOD前缀的群订单菜品ID字符串
     */
    public static String toGroupOrderDishIdString(Long groupOrderDishId) {
        if (groupOrderDishId == null) {
            return null;
        }
        return GROUP_ORDER_DISH_ID_PREFIX + groupOrderDishId;
    }

    /**
     * 将订单菜品ID转换为带OD前缀的字符串
     * @param orderDishId 订单菜品ID
     * @return 带OD前缀的订单菜品ID字符串
     */
    public static String toOrderDishIdString(Long orderDishId) {
        if (orderDishId == null) {
            return null;
        }
        return ORDER_DISH_ID_PREFIX + orderDishId;
    }

    /**
     * 将评价回复ID转换为带RRP前缀的字符串
     * @param reviewReplyId 评价回复ID
     * @return 带RRP前缀的评价回复ID字符串
     */
    public static String toReviewReplyIdString(Long reviewReplyId) {
        if (reviewReplyId == null) {
            return null;
        }
        return REVIEW_REPLY_ID_PREFIX + reviewReplyId;
    }

    /**
     * 将菜单菜品ID转换为带MND前缀的字符串
     * @param menuDishId 菜单菜品ID
     * @return 带MND前缀的菜单菜品ID字符串
     */
    public static String toMenuDishIdString(Long menuDishId) {
        if (menuDishId == null) {
            return null;
        }
        return MENU_DISH_ID_PREFIX + menuDishId;
    }

    /**
     * 从带前缀的ID字符串中提取原始Long类型ID
     * @param prefixedId 带前缀的ID字符串
     * @return 原始Long类型ID
     */
    public static Long toLongId(String prefixedId) {
        if (prefixedId == null || prefixedId.length() < 2) {
            throw new IllegalArgumentException("无效的ID格式: " + prefixedId);
        }
        // 提取除第一位前缀外的部分
        String idPart = prefixedId.substring(1);
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的ID格式: " + prefixedId, e);
        }
    }

    /**
     * 从带前缀的ID字符串中提取ID类型
     * @param prefixedId 带前缀的ID字符串
     * @return ID类型 (USER/MERCHANT/GROUP/DISH/MENU/ORDER等)
     */
    public static String extractIdType(String prefixedId) {
        if (prefixedId == null || prefixedId.length() < 1) {
            throw new IllegalArgumentException("无效的ID格式");
        }

        // 检查多字符前缀
        if (prefixedId.startsWith(USER_ID_PREFIX)) {
            return "USER";
        } else if (prefixedId.startsWith(MERCHANT_ID_PREFIX)) {
            return "MERCHANT";
        } else if (prefixedId.startsWith(GROUP_ID_PREFIX)) {
            return "GROUP";
        } else if (prefixedId.startsWith(DISH_ID_PREFIX)) {
            return "DISH";
        } else if (prefixedId.startsWith(MENU_ID_PREFIX)) {
            return "MENU";
        } else if (prefixedId.startsWith(ORDER_ID_PREFIX)) {
            return "ORDER";
        } else if (prefixedId.startsWith(ADDRESS_ID_PREFIX)) {
            return "ADDRESS";
        } else if (prefixedId.startsWith(PAYMENT_ID_PREFIX)) {
            return "PAYMENT";
        } else if (prefixedId.startsWith(REVIEW_ID_PREFIX)) {
            // 需要和RECIPE区分，REVIEW是R，RECIPE是RC
            if (prefixedId.startsWith(RECIPE_ID_PREFIX)) {
                return "RECIPE";
            }
            return "REVIEW";
        } else if (prefixedId.startsWith(WALLET_ID_PREFIX)) {
            return "WALLET";
        } else if (prefixedId.startsWith(NOTIFICATION_ID_PREFIX)) {
            return "NOTIFICATION";
        } else if (prefixedId.startsWith(COLLECTION_ID_PREFIX)) {
            return "COLLECTION";
        } else if (prefixedId.startsWith(CONTACT_ID_PREFIX)) {
            return "CONTACT";
        } else if (prefixedId.startsWith(CHAT_MSG_ID_PREFIX)) {
            return "CHAT_MSG";
        } else if (prefixedId.startsWith(MESSAGE_RECORD_ID_PREFIX)) {
            return "MESSAGE_RECORD";
        } else if (prefixedId.startsWith(ANNOUNCEMENT_ID_PREFIX)) {
            return "ANNOUNCEMENT";
        } else if (prefixedId.startsWith(TUTORIAL_ID_PREFIX)) {
            return "TUTORIAL";
        } else if (prefixedId.startsWith(CONSUME_HISTORY_ID_PREFIX)) {
            return "CONSUME_HISTORY";
        } else if (prefixedId.startsWith(RECHARGE_RECORD_ID_PREFIX)) {
            return "RECHARGE_RECORD";
        } else if (prefixedId.startsWith(WITHDRAW_RECORD_ID_PREFIX)) {
            return "WITHDRAW_RECORD";
        } else if (prefixedId.startsWith(USER_PREFERENCE_ID_PREFIX)) {
            return "USER_PREFERENCE";
        } else if (prefixedId.startsWith(CALORIE_RECORD_ID_PREFIX)) {
            return "CALORIE_RECORD";
        } else if (prefixedId.startsWith(GROUP_ORDER_ID_PREFIX)) {
            return "GROUP_ORDER";
        } else if (prefixedId.startsWith(GROUP_ORDER_DISH_ID_PREFIX)) {
            return "GROUP_ORDER_DISH";
        } else if (prefixedId.startsWith(ORDER_DISH_ID_PREFIX)) {
            return "ORDER_DISH";
        } else if (prefixedId.startsWith(REVIEW_REPLY_ID_PREFIX)) {
            return "REVIEW_REPLY";
        } else if (prefixedId.startsWith(MENU_DISH_ID_PREFIX)) {
            return "MENU_DISH";
        } else {
            throw new IllegalArgumentException("未知的ID前缀: " + prefixedId);
        }
    }

    /**
     * 验证带前缀的ID格式是否有效
     * @param prefixedId 带前缀的ID字符串
     * @return 是否是有效的ID
     */
    public static boolean isValidId(String prefixedId) {
        if (prefixedId == null || prefixedId.length() < ID_LENGTH + 1) {
            return false;
        }

        // 检查是否以有效的前缀开头
        String[] validPrefixes = {
            USER_ID_PREFIX, MERCHANT_ID_PREFIX, GROUP_ID_PREFIX,
            DISH_ID_PREFIX, MENU_ID_PREFIX, ORDER_ID_PREFIX,
            ADDRESS_ID_PREFIX, PAYMENT_ID_PREFIX, REVIEW_ID_PREFIX,
            WALLET_ID_PREFIX, NOTIFICATION_ID_PREFIX, RECIPE_ID_PREFIX,
            COLLECTION_ID_PREFIX, CONTACT_ID_PREFIX, CHAT_MSG_ID_PREFIX,
            MESSAGE_RECORD_ID_PREFIX, ANNOUNCEMENT_ID_PREFIX, TUTORIAL_ID_PREFIX,
            CONSUME_HISTORY_ID_PREFIX, RECHARGE_RECORD_ID_PREFIX, WITHDRAW_RECORD_ID_PREFIX,
            USER_PREFERENCE_ID_PREFIX, CALORIE_RECORD_ID_PREFIX, GROUP_ORDER_ID_PREFIX,
            GROUP_ORDER_DISH_ID_PREFIX, ORDER_DISH_ID_PREFIX, REVIEW_REPLY_ID_PREFIX,
            MENU_DISH_ID_PREFIX
        };

        boolean hasValidPrefix = false;
        int prefixLength = 0;
        for (String prefix : validPrefixes) {
            if (prefixedId.startsWith(prefix)) {
                hasValidPrefix = true;
                prefixLength = prefix.length();
                break;
            }
        }

        if (!hasValidPrefix) {
            return false;
        }

        // 验证前缀之后的部分是否都是数字
        for (int i = prefixLength; i < prefixedId.length(); i++) {
            if (!Character.isDigit(prefixedId.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
