package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.dto.OrderDishVO;
import com.xx.jaseatschoicejava.dto.ReorderItemDTO;
import com.xx.jaseatschoicejava.dto.ReorderResponseDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.mapper.OrderMapper;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderDishService orderDishService;
    private final DishService dishService;
    private final MerchantService merchantService;

    /**
     * 创建订单并保存订单菜品(事务方法)
     * @param order 订单信息
     * @param orderDishes 订单菜品列表
     * @return 是否创建成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrderWithDishes(Order order, List<OrderDish> orderDishes) {
        try {
            // 如果订单ID为空，自动生成
            if (order.getId() == null || order.getId().isEmpty()) {
                // 使用 IdGenerator 生成订单ID
                Long generatedId = com.xx.jaseatschoicejava.util.IdGenerator.generateId();
                String orderId = com.xx.jaseatschoicejava.util.IdGenerator.toOrderIdString(generatedId);
                order.setId(orderId);
                log.info("自动生成订单ID: {}", orderId);
            }

            log.info("开始创建订单,订单ID: {}, 菜品数量: {}", order.getId(),
                    orderDishes != null ? orderDishes.size() : 0);

            // 1. 保存订单
            boolean orderSaved = this.save(order);
            if (!orderSaved) {
                log.error("保存订单失败,订单ID: {}", order.getId());
                return false;
            }
            log.info("订单保存成功,订单ID: {}", order.getId());

            // 2. 保存订单菜品(如果有)
            if (orderDishes != null && !orderDishes.isEmpty()) {
                // 为每个菜品设置订单ID
                for (OrderDish orderDish : orderDishes) {
                    orderDish.setOrderId(order.getId());
                }

                // 批量保存订单菜品
                boolean dishesSaved = orderDishService.saveBatch(orderDishes);
                if (!dishesSaved) {
                    log.error("保存订单菜品失败,订单ID: {}", order.getId());
                    throw new RuntimeException("保存订单菜品失败");
                }
                log.info("订单菜品保存成功,订单ID: {}, 菜品数量: {}", order.getId(), orderDishes.size());
            }

            log.info("订单和菜品创建成功,订单ID: {}", order.getId());
            return true;
        } catch (Exception e) {
            log.error("创建订单和菜品失败,订单ID: {}", order.getId(), e);
            throw new RuntimeException("创建订单和菜品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 再来一单 - 智能复购
     * @param orderId 原订单ID
     * @return 再来一单响应数据
     */
    @Override
    public ReorderResponseDTO reorder(String orderId) {
        log.info("开始处理再来一单请求，订单ID: {}", orderId);

        // 1. 获取原订单信息
        Order originalOrder = this.getById(orderId);
        if (originalOrder == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 获取商家信息
        Merchant merchant = merchantService.getById(originalOrder.getMerchantId());
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }

        // 3. 获取订单菜品列表
        List<OrderDishVO> orderDishes = orderDishService.getOrderDishesWithDetails(orderId);
        if (orderDishes == null || orderDishes.isEmpty()) {
            throw new RuntimeException("订单菜品为空");
        }

        // 4. 构建响应对象
        ReorderResponseDTO response = new ReorderResponseDTO();
        response.setOriginalOrderId(orderId);
        response.setMerchantId(originalOrder.getMerchantId());
        response.setMerchantName(merchant.getName());
        response.setOriginalTotalAmount(originalOrder.getTotalAmount());
        response.setOriginalRemark(originalOrder.getRemark());
        response.setOriginalAddressId(originalOrder.getAddressId());
        response.setOriginalAddress(originalOrder.getAddress());

        // 5. 处理每个菜品
        List<ReorderItemDTO> items = new ArrayList<>();
        BigDecimal currentTotalAmount = BigDecimal.ZERO;
        int soldOutCount = 0;
        int priceIncreasedCount = 0;
        int priceDecreasedCount = 0;
        int normalCount = 0;

        for (OrderDishVO orderDish : orderDishes) {
            ReorderItemDTO item = processOrderDish(orderDish);
            items.add(item);

            // 统计
            if (item.getDishStatus() == 1) {
                soldOutCount++;
            } else if (item.getDishStatus() == 0) {
                if (item.getIsPriceIncreased()) {
                    priceIncreasedCount++;
                } else if (item.getCurrentPrice().compareTo(item.getOriginalPrice()) < 0) {
                    priceDecreasedCount++;
                } else {
                    normalCount++;
                }
            }

            // 累计当前总价（只计算可选择的菜品）
            if (item.getCanSelect() && item.getDishStatus() == 0) {
                BigDecimal itemTotal = item.getCurrentPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                currentTotalAmount = currentTotalAmount.add(itemTotal);
            }
        }

        response.setItems(items);
        response.setCurrentTotalAmount(currentTotalAmount);

        // 6. 设置金额变动说明
        if (currentTotalAmount.compareTo(originalOrder.getTotalAmount()) > 0) {
            BigDecimal increase = currentTotalAmount.subtract(originalOrder.getTotalAmount());
            response.setAmountChangeNote(String.format("价格变动：总价增加¥%s", increase.setScale(2, RoundingMode.HALF_UP)));
        } else if (currentTotalAmount.compareTo(originalOrder.getTotalAmount()) < 0) {
            BigDecimal decrease = originalOrder.getTotalAmount().subtract(currentTotalAmount);
            response.setAmountChangeNote(String.format("价格变动：总价减少¥%s", decrease.setScale(2, RoundingMode.HALF_UP)));
        } else {
            response.setAmountChangeNote("价格变动：总价无变化");
        }

        // 7. 设置统计信息
        response.setSoldOutCount(soldOutCount);
        response.setPriceIncreasedCount(priceIncreasedCount);
        response.setPriceDecreasedCount(priceDecreasedCount);
        response.setNormalCount(normalCount);
        response.setHasChanges(soldOutCount > 0 || priceIncreasedCount > 0 || priceDecreasedCount > 0);
        response.setAllItemsUnavailable(normalCount == 0 && soldOutCount == orderDishes.size());

        log.info("再来一单处理完成，订单ID: {}, 原总价: {}, 当前总价: {}",
            orderId, originalOrder.getTotalAmount(), currentTotalAmount);

        return response;
    }

    /**
     * 处理单个订单菜品
     */
    private ReorderItemDTO processOrderDish(OrderDishVO orderDish) {
        ReorderItemDTO item = new ReorderItemDTO();
        item.setOrderDishId(orderDish.getId());
        item.setDishId(orderDish.getDishId());
        item.setDishName(orderDish.getDish() != null ? orderDish.getDish().getName() : "未知菜品");
        item.setDishImage(orderDish.getDish() != null ? orderDish.getDish().getImage() : "");
        item.setQuantity(orderDish.getQuantity());
        item.setOriginalPrice(orderDish.getPrice());
        item.setCustomization(orderDish.getCustomization());

        // 获取当前菜品信息
        Dish currentDish = dishService.getById(orderDish.getDishId());

        if (currentDish == null || !currentDish.getIsOnline()) {
            // 菜品已下架
            item.setDishStatus(1);
            item.setStatusDescription("sold_out");
            item.setCanSelect(false);
            item.setDefaultSelected(false);

            // 查找相似菜品推荐
            Dish similarDish = findSimilarDish(orderDish.getDishId(), currentDish);
            if (similarDish != null) {
                item.setSuggestedDishId(similarDish.getId());
                item.setSuggestedDishName(similarDish.getName());
                item.setSuggestedDishPrice(similarDish.getPrice());
                item.setSuggestedDishImage(similarDish.getImage());
                item.setSuggestionReason("原菜品已下架，推荐相似菜品");
            } else {
                item.setSuggestionReason("原菜品已下架，暂无相似菜品");
            }
        } else if (currentDish.getStock() != null && currentDish.getStock() < orderDish.getQuantity()) {
            // 库存不足
            item.setDishStatus(2);
            item.setStatusDescription("out_of_stock");
            item.setCanSelect(false);
            item.setDefaultSelected(false);
            item.setSuggestionReason("库存不足");
        } else {
            // 菜品正常，检查价格变动
            item.setDishStatus(0);
            item.setStatusDescription("normal");
            item.setCurrentPrice(currentDish.getPrice());
            item.setCanSelect(true);
            item.setDefaultSelected(true);

            // 检查价格变动
            int priceCompare = currentDish.getPrice().compareTo(orderDish.getPrice());
            if (priceCompare > 0) {
                // 涨价
                item.setIsPriceIncreased(true);
                BigDecimal increaseRate = currentDish.getPrice()
                    .subtract(orderDish.getPrice())
                    .divide(orderDish.getPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                item.setPriceIncreaseRate(increaseRate);
                item.setPriceChangeNote(String.format("原¥%s，现价¥%s（涨价%.1f%%）",
                    orderDish.getPrice(),
                    currentDish.getPrice(),
                    increaseRate.setScale(1, RoundingMode.HALF_UP)));
            } else if (priceCompare < 0) {
                // 降价
                item.setIsPriceIncreased(false);
                item.setPriceChangeNote(String.format("原¥%s，现价¥%s（降价）",
                    orderDish.getPrice(),
                    currentDish.getPrice()));
            } else {
                // 价格无变化
                item.setIsPriceIncreased(false);
                item.setPriceChangeNote("价格无变化");
            }
        }

        return item;
    }

    /**
     * 查找相似菜品
     * 简化实现：查找同商家同分类的菜品
     */
    private Dish findSimilarDish(String originalDishId, Dish originalDish) {
        if (originalDish == null) {
            return null;
        }

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getMerchantId, originalDish.getMerchantId());
        queryWrapper.eq(Dish::getIsOnline, true);
        queryWrapper.ne(Dish::getId, originalDishId);

        // 优先查找同分类的菜品
        if (originalDish.getCategory() != null && !originalDish.getCategory().isEmpty()) {
            queryWrapper.eq(Dish::getCategory, originalDish.getCategory());
            queryWrapper.orderByDesc(Dish::getAvgRating);
            queryWrapper.last("LIMIT 1");

            List<Dish> similarDishes = dishService.list(queryWrapper);
            if (!similarDishes.isEmpty()) {
                return similarDishes.get(0);
            }
        }

        // 如果同分类没有找到，查找同一商家评分最高的菜品
        queryWrapper.clear();
        queryWrapper.eq(Dish::getMerchantId, originalDish.getMerchantId());
        queryWrapper.eq(Dish::getIsOnline, true);
        queryWrapper.ne(Dish::getId, originalDishId);
        queryWrapper.orderByDesc(Dish::getAvgRating);
        queryWrapper.last("LIMIT 1");

        List<Dish> similarDishes = dishService.list(queryWrapper);
        return similarDishes.isEmpty() ? null : similarDishes.get(0);
    }

    /**
     * 订单状态回退实现
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackStatus(String orderId, Integer targetStatus, String reason, String operatorId) {
        log.info("订单状态回退请求 - orderId: {}, targetStatus: {}, reason: {}, operatorId: {}",
                orderId, targetStatus, reason, operatorId);

        // 1. 查询订单信息
        Order order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        Integer currentStatus = order.getStatus();

        // 2. 检查状态流转是否合法
        if (!isValidRollback(currentStatus, targetStatus)) {
            throw new RuntimeException("不允许从状态" + currentStatus + "回退到状态" + targetStatus);
        }

        // 3. 检查目标状态是否为终态
        if (isFinalStatus(targetStatus)) {
            throw new RuntimeException("不能回退到终态：" + targetStatus);
        }

        // 4. 更新订单状态
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, orderId)
                .set(Order::getStatus, targetStatus)
                .set(Order::getUpdateTime, LocalDateTime.now());

        int updated = this.getBaseMapper().update(null, updateWrapper);

        if (updated > 0) {
            log.info("订单状态回退成功 - orderId: {}, {} -> {}, reason: {}",
                    orderId, currentStatus, targetStatus);

            // 根据回退后的状态发送通知
            String statusText = getStatusText(targetStatus);
            NotificationUtil.createOrderNotification(
                order.getUserId(),
                NotificationTypeEnum.ORDER_CANCELLED,
                orderId,
                "状态回退至：" + statusText
            );
        }

        return updated > 0;
    }

    @Override
    public List<Integer> getRollbackOptions(Integer currentStatus) {
        // 根据当前状态返回可以回退到的状态列表
        // 规则：只能回退到之前的普通流转状态，不能跳过中间状态
        switch (currentStatus) {
            case 0: // 待支付 - 不能回退
                return new ArrayList<>();
            case 1: // 待接单 - 可回退到待支付
                return List.of(0);
            case 2: // 备菜中 - 可回退到待接单
                return List.of(1);
            case 3: // 烹饪中 - 可回退到备菜中
                return List.of(2);
            case 4: // 待上菜 - 可回退到烹饪中
                return List.of(3);
            case 5: // 已完成 - 可回退到待上菜
                return List.of(4);
            case 6: // 已取消 - 不能回退
            return new ArrayList<>();
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 检查状态回退是否合法
     */
    private boolean isValidRollback(Integer fromStatus, Integer toStatus) {
        // 规则：
        // 1. 不能从已取消回退
        // 2. 不能跳过状态（例如不能从烹饪中直接回退到待支付）
        // 3. 只能回退到相邻的前一状态

        if (fromStatus == 6) { // 已取消
            return false; // 已取消不能回退
        }

        // 只能回退到前一状态
        return toStatus == fromStatus - 1;
    }

    /**
     * 判断是否为终态（终态不能回退）
     */
    private boolean isFinalStatus(Integer status) {
        // 终态：已完成、已取消
        return status == 3 || status == 4;
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待支付";
            case 1: return "待接单";
            case 2: return "制作中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    // ==================== 缓存相关方法 ====================

    private static final String CACHE_NAME = "order:detail";

    /**
     * 获取订单详情（带缓存）
     *
     * 注意：只读场景使用缓存，订单状态变化快
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @org.springframework.cache.annotation.Cacheable(
        value = CACHE_NAME,
        key = "#orderId",
        unless = "#result == null"
    )
    public Order getOrderById(String orderId) {
        log.debug("从数据库查询订单详情: orderId={}", orderId);
        return getById(orderId);
    }

    /**
     * 更新订单状态并清除缓存
     *
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    @org.springframework.cache.annotation.CacheEvict(value = CACHE_NAME, key = "#orderId")
    public boolean updateOrderStatus(String orderId, Integer status) {
        log.debug("更新订单状态并清除缓存: orderId={}, status={}", orderId, status);
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, orderId)
                .set(Order::getStatus, status)
                .set(Order::getUpdateTime, LocalDateTime.now());
        return update(updateWrapper);
    }

    /**
     * 清除订单缓存
     *
     * @param orderId 订单ID
     */
    @org.springframework.cache.annotation.CacheEvict(value = CACHE_NAME, key = "#orderId")
    public void evictOrderCache(String orderId) {
        log.debug("清除订单缓存: orderId={}", orderId);
    }

    /**
     * 获取用户各状态订单数量统计
     * @param userId 用户ID
     * @return Map<状态名称, 数量>
     */
    @Override
    public Map<String, Long> getOrderCountByUserId(String userId) {
        log.debug("统计用户订单数量: userId={}", userId);

        Map<String, Long> countMap = new HashMap<>();

        try {
            // 查询所有订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, userId);
            List<Order> orders = list(queryWrapper);

            // 统计各状态订单数量
            Map<Integer, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

            // 映射到前端需要的字段名
            countMap.put("pending", statusCounts.getOrDefault(0, 0L));           // 待支付
            countMap.put("processing", statusCounts.getOrDefault(1, 0L));        // 待接单
            countMap.put("delivering", statusCounts.getOrDefault(2, 0L));         // 制作中
            countMap.put("completed", statusCounts.getOrDefault(3, 0L));          // 已完成

            log.debug("订单统计结果: {}", countMap);
        } catch (Exception e) {
            log.error("统计用户订单数量失败: userId={}", userId, e);
            // 返回空统计
            countMap.put("pending", 0L);
            countMap.put("processing", 0L);
            countMap.put("delivering", 0L);
            countMap.put("completed", 0L);
        }

        return countMap;
    }
}
