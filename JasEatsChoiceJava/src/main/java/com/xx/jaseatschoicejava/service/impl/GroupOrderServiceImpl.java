package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.GroupOrderDishMapper;
import com.xx.jaseatschoicejava.mapper.GroupOrderMapper;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 群订单服务实现
 */
@Service
public class GroupOrderServiceImpl extends ServiceImpl<GroupOrderMapper, GroupOrder> implements GroupOrderService {

    @Autowired
    private GroupOrderDishMapper groupOrderDishMapper;

    @Autowired
    private DishService dishService;

    @Autowired
    private UserService userService;

    /**
     * 创建群订单，使用事务确保数据一致性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createGroupOrder(GroupOrder groupOrder, List<GroupOrderDish> dishItems) {
        // 设置默认值
        groupOrder.setCreateTime(LocalDateTime.now());
        groupOrder.setUpdateTime(LocalDateTime.now());
        if (groupOrder.getStatus() == null) {
            groupOrder.setStatus(0); // 默认待支付状态
        }

        // 保存群订单
        boolean orderSaved = save(groupOrder);
        if (!orderSaved) {
            return false;
        }

        // 保存群订单菜品
        boolean dishesSaved = true;
        if (dishItems != null) {
            for (GroupOrderDish dish : dishItems) {
                dish.setGroupOrderId(groupOrder.getId());
                if (groupOrderDishMapper.insert(dish) <= 0) {
                    dishesSaved = false;
                    break;
                }
            }
        }
        if (!dishesSaved) {
            // 事务会自动回滚
            return false;
        }

        return true;
    }

    @Override
    public List<GroupOrder> getGroupOrdersByGroupId(String groupId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<GroupOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrder::getGroupId, groupId);
        queryWrapper.orderByDesc(GroupOrder::getCreateTime);

        // 状态筛选
        if (status != null) {
            queryWrapper.eq(GroupOrder::getStatus, status);
        }

        // 分页（MyBatis Plus的page方法自动处理分页）
        // 计算起始索引
        int startIndex = (page - 1) * size;
        return list(queryWrapper)
                .stream()
                .skip(startIndex)
                .limit(size)
                .toList();
    }

    @Override
    public GroupOrder getGroupOrderDetail(String groupOrderId) {
        return getById(groupOrderId);
    }

    @Override
    public List<GroupOrderDish> getGroupOrderDishes(String groupOrderId) {
        LambdaQueryWrapper<GroupOrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderDish::getGroupOrderId, groupOrderId);
        return groupOrderDishMapper.selectList(queryWrapper);
    }

    @Override
    public List<GroupOrder> getUserGroupOrders(String userId, Integer status, Integer page, Integer size) {
        LinkedHashMap<String, GroupOrder> orderMap = new LinkedHashMap<>();

        LambdaQueryWrapper<GroupOrder> initiatorQuery = new LambdaQueryWrapper<>();
        initiatorQuery.eq(GroupOrder::getInitiatorId, userId);
        if (status != null) {
            initiatorQuery.eq(GroupOrder::getStatus, status);
        }
        initiatorQuery.orderByDesc(GroupOrder::getCreateTime);
        list(initiatorQuery).forEach(order -> orderMap.put(order.getId(), order));

        LambdaQueryWrapper<GroupOrderDish> dishQuery = new LambdaQueryWrapper<>();
        dishQuery.eq(GroupOrderDish::getUserId, userId);
        List<GroupOrderDish> joinedDishes = groupOrderDishMapper.selectList(dishQuery);
        Set<String> joinedOrderIds = new LinkedHashSet<>();
        joinedDishes.forEach(item -> joinedOrderIds.add(item.getGroupOrderId()));

        if (!joinedOrderIds.isEmpty()) {
            LambdaQueryWrapper<GroupOrder> joinedOrderQuery = new LambdaQueryWrapper<>();
            joinedOrderQuery.in(GroupOrder::getId, joinedOrderIds);
            if (status != null) {
                joinedOrderQuery.eq(GroupOrder::getStatus, status);
            }
            joinedOrderQuery.orderByDesc(GroupOrder::getCreateTime);
            list(joinedOrderQuery).forEach(order -> orderMap.putIfAbsent(order.getId(), order));
        }

        return orderMap.values().stream()
                .sorted(Comparator.comparing(GroupOrder::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .skip((long) Math.max(page - 1, 0) * size)
                .limit(size)
                .toList();
    }

    @Override
    public Map<String, Object> joinByOrderCode(String orderCode, String userId) {
        GroupOrder targetOrder = list().stream()
                .filter(order -> Objects.equals(buildOrderCode(order.getId()), orderCode))
                .findFirst()
                .orElse(null);

        if (targetOrder == null) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groupOrderId", targetOrder.getId());
        result.put("orderId", targetOrder.getId());
        result.put("orderCode", buildOrderCode(targetOrder.getId()));
        result.put("groupId", targetOrder.getGroupId());
        result.put("merchantId", targetOrder.getMerchantId());
        result.put("status", targetOrder.getStatus());
        result.put("joined", hasUserJoined(targetOrder.getId(), userId));
        result.put("message", "订单码有效，可进入拼单详情");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> saveUserSelections(String groupOrderId, String userId, List<Map<String, Object>> dishes) {
        LambdaQueryWrapper<GroupOrderDish> removeQuery = new LambdaQueryWrapper<>();
        removeQuery.eq(GroupOrderDish::getGroupOrderId, groupOrderId)
                .eq(GroupOrderDish::getUserId, userId);
        groupOrderDishMapper.delete(removeQuery);

        if (dishes != null) {
            for (Map<String, Object> item : dishes) {
                Integer quantity = parseInteger(item.get("quantity"));
                if (quantity == null || quantity <= 0) {
                    continue;
                }

                Object dishIdValue = item.get("dishId");
                if (dishIdValue == null || dishIdValue.toString().isBlank()) {
                    continue;
                }

                GroupOrderDish selection = new GroupOrderDish();
                selection.setGroupOrderId(groupOrderId);
                selection.setUserId(userId);
                selection.setDishId(dishIdValue.toString());
                selection.setQuantity(quantity);
                Object customization = item.get("customization") != null
                        ? item.get("customization")
                        : item.get("specification");
                selection.setCustomization(customization == null ? null : customization.toString());
                groupOrderDishMapper.insert(selection);
            }
        }

        GroupOrder groupOrder = getById(groupOrderId);
        if (groupOrder != null) {
            groupOrder.setUpdateTime(LocalDateTime.now());
            updateById(groupOrder);
        }

        return getUserSelections(groupOrderId, userId);
    }

    @Override
    public List<Map<String, Object>> getUserSelections(String groupOrderId, String userId) {
        LambdaQueryWrapper<GroupOrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderDish::getGroupOrderId, groupOrderId)
                .eq(GroupOrderDish::getUserId, userId);
        queryWrapper.orderByAsc(GroupOrderDish::getId);

        List<GroupOrderDish> selections = groupOrderDishMapper.selectList(queryWrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupOrderDish selection : selections) {
            result.add(buildSelectionItem(selection));
        }
        return result;
    }

    @Override
    public Map<String, Object> getSettlement(String groupOrderId, String userId) {
        List<Map<String, Object>> selections = getUserSelections(groupOrderId, userId);
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map<String, Object> selection : selections) {
            BigDecimal price = selection.get("price") instanceof BigDecimal
                    ? (BigDecimal) selection.get("price")
                    : BigDecimal.valueOf(Double.parseDouble(String.valueOf(selection.getOrDefault("price", 0))));
            Integer quantity = parseInteger(selection.get("quantity"));
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(quantity == null ? 0 : quantity)));
        }

        GroupOrder groupOrder = getById(groupOrderId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("subtotal", subtotal);
        result.put("deliveryFee", BigDecimal.ZERO);
        result.put("packagingFee", BigDecimal.ZERO);
        result.put("discount", BigDecimal.ZERO);
        result.put("totalAmount", subtotal);
        result.put("orderId", groupOrderId);
        result.put("merchantId", groupOrder == null ? null : groupOrder.getMerchantId());
        result.put("items", selections);
        return result;
    }

    private boolean hasUserJoined(String groupOrderId, String userId) {
        GroupOrder groupOrder = getById(groupOrderId);
        if (groupOrder != null && Objects.equals(groupOrder.getInitiatorId(), userId)) {
            return true;
        }

        LambdaQueryWrapper<GroupOrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderDish::getGroupOrderId, groupOrderId)
                .eq(GroupOrderDish::getUserId, userId);
        return groupOrderDishMapper.selectCount(queryWrapper) > 0;
    }

    private Map<String, Object> buildSelectionItem(GroupOrderDish selection) {
        Dish dish = dishService.getById(selection.getDishId());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", selection.getId());
        item.put("dishId", selection.getDishId());
        item.put("quantity", selection.getQuantity());
        item.put("customization", selection.getCustomization());
        item.put("specification", selection.getCustomization());
        item.put("name", dish == null ? "菜品" + selection.getDishId() : dish.getName());
        item.put("dishName", dish == null ? "菜品" + selection.getDishId() : dish.getName());
        item.put("image", dish == null ? null : dish.getImage());
        item.put("price", dish == null || dish.getPrice() == null ? BigDecimal.ZERO : dish.getPrice());
        item.put("userId", selection.getUserId());

        User user = userService.getById(selection.getUserId());
        if (user != null) {
            item.put("userName", user.getNickname());
            item.put("avatar", user.getAvatar());
        }
        return item;
    }

    private String buildOrderCode(String groupOrderId) {
        String digits = groupOrderId == null ? "" : groupOrderId.replaceAll("\\D", "");
        if (digits.length() > 6) {
            digits = digits.substring(digits.length() - 6);
        }
        return String.format("%6s", digits).replace(' ', '0');
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.valueOf(value.toString());
    }
}
