package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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

        if (currentDish == null || !currentDish.getStatus()) {
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
        queryWrapper.eq(Dish::getStatus, true);
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
        queryWrapper.eq(Dish::getStatus, true);
        queryWrapper.ne(Dish::getId, originalDishId);
        queryWrapper.orderByDesc(Dish::getAvgRating);
        queryWrapper.last("LIMIT 1");

        List<Dish> similarDishes = dishService.list(queryWrapper);
        return similarDishes.isEmpty() ? null : similarDishes.get(0);
    }
}
