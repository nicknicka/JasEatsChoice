package com.xx.jaseatschoicejava.util;

import com.xx.jaseatschoicejava.dto.UserBehaviorDTO;
import com.xx.jaseatschoicejava.service.UserBehaviorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 行为记录工具类
 * 简化控制器中的行为记录逻辑
 */
public class BehaviorTrackingUtil {

    private static final Logger log = LoggerFactory.getLogger(BehaviorTrackingUtil.class);

    /**
     * 异步记录菜品浏览行为
     *
     * @param request HTTP请求
     * @param userBehaviorService 用户行为服务
     * @param dishId 菜品ID
     * @param behaviorType 行为类型（view/click/favorite/order/reject/share）
     * @param additionalContext 额外的上下文信息
     */
    public static void trackDishBehavior(
            HttpServletRequest request,
            UserBehaviorService userBehaviorService,
            String dishId,
            String behaviorType,
            Map<String, Object> additionalContext) {

        try {
            // 获取用户ID（可选）
            String userId = UserIdentityUtil.extractUserId(request);
            if (userId == null) {
                log.debug("未获取到用户ID，跳过行为记录");
                return;
            }

            // 构建上下文信息
            Map<String, Object> context = new HashMap<>();
            context.put("userAgent", request.getHeader("User-Agent"));
            context.put("referer", request.getHeader("Referer"));
            context.put("timestamp", System.currentTimeMillis());

            // 添加额外的上下文信息
            if (additionalContext != null) {
                context.putAll(additionalContext);
            }

            // 创建行为记录DTO
            UserBehaviorDTO behaviorDTO = new UserBehaviorDTO();
            behaviorDTO.setUserId(userId);
            behaviorDTO.setBehaviorType(behaviorType);
            behaviorDTO.setItemType("dish");
            behaviorDTO.setItemId(dishId);
            behaviorDTO.setContext(context);

            // 异步记录
            userBehaviorService.recordBehaviorAsync(behaviorDTO);

            log.debug("记录用户行为成功：userId={}, type={}, dish={}", userId, behaviorType, dishId);

        } catch (Exception e) {
            // 记录失败不影响主流程
            log.error("记录用户行为失败：dishId={}, type={}", dishId, behaviorType, e);
        }
    }

    /**
     * 记录菜品列表浏览行为
     *
     * @param request HTTP请求
     * @param userBehaviorService 用户行为服务
     * @param listType 列表类型（search_list/merchant_dish_list/category_list等）
     * @param itemCount 列表中的菜品数量
     * @param additionalContext 额外的上下文信息
     */
    public static void trackDishListView(
            HttpServletRequest request,
            UserBehaviorService userBehaviorService,
            String listType,
            int itemCount,
            Map<String, Object> additionalContext) {

        try {
            String userId = UserIdentityUtil.extractUserId(request);
            if (userId == null) {
                log.debug("未获取到用户ID，跳过列表浏览记录");
                return;
            }

            Map<String, Object> context = new HashMap<>();
            context.put("listType", listType);
            context.put("itemCount", itemCount);
            context.put("userAgent", request.getHeader("User-Agent"));
            context.put("referer", request.getHeader("Referer"));
            context.put("timestamp", System.currentTimeMillis());

            if (additionalContext != null) {
                context.putAll(additionalContext);
            }

            UserBehaviorDTO behaviorDTO = new UserBehaviorDTO();
            behaviorDTO.setUserId(userId);
            behaviorDTO.setBehaviorType("view");
            behaviorDTO.setItemType("dish_list");
            behaviorDTO.setItemId(listType + "_" + System.currentTimeMillis());
            behaviorDTO.setContext(context);

            userBehaviorService.recordBehaviorAsync(behaviorDTO);

            log.debug("记录列表浏览成功：userId={}, listType={}, count={}", userId, listType, itemCount);

        } catch (Exception e) {
            log.error("记录列表浏览失败：listType={}, count={}", listType, itemCount, e);
        }
    }

    /**
     * 记录商家浏览行为
     *
     * @param request HTTP请求
     * @param userBehaviorService 用户行为服务
     * @param merchantId 商家ID
     * @param additionalContext 额外的上下文信息
     */
    public static void trackMerchantBehavior(
            HttpServletRequest request,
            UserBehaviorService userBehaviorService,
            String merchantId,
            Map<String, Object> additionalContext) {

        try {
            String userId = UserIdentityUtil.extractUserId(request);
            if (userId == null) {
                log.debug("未获取到用户ID，跳过商家浏览记录");
                return;
            }

            Map<String, Object> context = new HashMap<>();
            context.put("userAgent", request.getHeader("User-Agent"));
            context.put("referer", request.getHeader("Referer"));
            context.put("timestamp", System.currentTimeMillis());

            if (additionalContext != null) {
                context.putAll(additionalContext);
            }

            UserBehaviorDTO behaviorDTO = new UserBehaviorDTO();
            behaviorDTO.setUserId(userId);
            behaviorDTO.setBehaviorType("view");
            behaviorDTO.setItemType("merchant");
            behaviorDTO.setItemId(merchantId);
            behaviorDTO.setContext(context);

            userBehaviorService.recordBehaviorAsync(behaviorDTO);

            log.debug("记录商家浏览成功：userId={}, merchantId={}", userId, merchantId);

        } catch (Exception e) {
            log.error("记录商家浏览失败：merchantId={}", merchantId, e);
        }
    }
}
