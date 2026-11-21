package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.CartItem;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.CartItemMapper;
import com.xx.jaseatschoicejava.service.CartItemService;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

    private final DishService dishService;
    private final MerchantService merchantService;

    /** 单次加购数量上限，防止误传超大值把库存/结算算爆。 */
    private static final int MAX_QUANTITY = 999;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItem addItem(String userId, String dishId, String merchantId, Integer quantity,
                            List<String> optionalIngredients, String spec, String remark) {
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new BusinessException("404", "菜品不存在");
        }
        if (Boolean.FALSE.equals(dish.getIsOnline())) {
            throw new BusinessException("400", "该菜品已下架，无法加入购物车");
        }
        // 商家以菜品归属为准：前端传的 merchantId 只用于交叉校验，不信任其作为权威来源
        String resolvedMerchantId = dish.getMerchantId();
        if (resolvedMerchantId == null || resolvedMerchantId.isBlank()) {
            throw new BusinessException("400", "该菜品未关联商家，无法加入购物车");
        }
        if (merchantId != null && !merchantId.isBlank() && !merchantId.equals(resolvedMerchantId)) {
            throw new BusinessException("400", "菜品不属于所选商家");
        }

        int qty = quantity == null || quantity <= 0 ? 1 : Math.min(quantity, MAX_QUANTITY);
        String normalizedSpec = spec == null ? "" : spec.trim();
        String ingredientsJson = optionalIngredients == null || optionalIngredients.isEmpty()
            ? null
            : toJsonArray(optionalIngredients);

        CartItem existing = findByUniqueKey(userId, resolvedMerchantId, dishId, normalizedSpec);
        if (existing != null) {
            // 去重合并：同一菜品同一规格重复加购只增加数量（「再来一单」依赖这一点）
            int merged = Math.min(existing.getQuantity() + qty, MAX_QUANTITY);
            existing.setQuantity(merged);
            existing.setSelected(true);
            if (remark != null && !remark.isBlank()) {
                existing.setRemark(remark.trim());
            }
            if (ingredientsJson != null) {
                existing.setOptionalIngredients(ingredientsJson);
            }
            existing.setUpdateTime(LocalDateTime.now());
            if (!updateById(existing)) {
                throw new BusinessException("500", "加入购物车失败，请稍后重试");
            }
            return existing;
        }

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setMerchantId(resolvedMerchantId);
        item.setDishId(dishId);
        item.setSpec(normalizedSpec);
        item.setQuantity(qty);
        item.setOptionalIngredients(ingredientsJson);
        item.setRemark(remark == null ? null : remark.trim());
        item.setSelected(true);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        try {
            if (!save(item)) {
                throw new BusinessException("500", "加入购物车失败，请稍后重试");
            }
        } catch (DuplicateKeyException e) {
            // 并发加购撞上唯一键：把数量合并到已存在那行，而不是报错
            CartItem concurrent = findByUniqueKey(userId, resolvedMerchantId, dishId, normalizedSpec);
            if (concurrent == null) {
                throw new BusinessException("500", "加入购物车失败，请稍后重试");
            }
            concurrent.setQuantity(Math.min(concurrent.getQuantity() + qty, MAX_QUANTITY));
            concurrent.setUpdateTime(LocalDateTime.now());
            updateById(concurrent);
            return concurrent;
        }

        return item;
    }

    @Override
    public List<Map<String, Object>> listGroupedByMerchant(String userId) {
        List<CartItem> items = list(new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, userId)
            .orderByAsc(CartItem::getMerchantId)
            .orderByAsc(CartItem::getCreateTime));
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        // 按商家分组，保持插入顺序
        Map<String, List<CartItem>> grouped = new LinkedHashMap<>();
        for (CartItem item : items) {
            grouped.computeIfAbsent(item.getMerchantId(), key -> new ArrayList<>()).add(item);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<CartItem>> entry : grouped.entrySet()) {
            String merchantId = entry.getKey();
            Merchant merchant = merchantService.getById(merchantId);

            List<Map<String, Object>> itemViews = new ArrayList<>();
            for (CartItem item : entry.getValue()) {
                // 菜品可能已删除；此时仍返回条目（带 dishId），由前端提示失效，而不是静默丢掉
                Dish dish = dishService.getById(item.getDishId());
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("id", item.getId());
                view.put("dishId", item.getDishId());
                view.put("name", dish == null ? "" : dish.getName());
                view.put("price", dish == null ? BigDecimal.ZERO : dish.getPrice());
                view.put("image", dish == null ? "" : dish.getImage());
                view.put("spec", item.getSpec());
                view.put("quantity", item.getQuantity());
                view.put("ingredients", parseJsonArray(item.getOptionalIngredients()));
                view.put("remark", item.getRemark());
                view.put("selected", item.getSelected());
                view.put("available", dish != null && !Boolean.FALSE.equals(dish.getIsOnline()));
                itemViews.add(view);
            }

            Map<String, Object> group = new LinkedHashMap<>();
            group.put("merchantId", merchantId);
            group.put("merchantName", merchant == null ? "" : merchant.getName());
            group.put("merchantLogo", merchant == null ? "" : merchant.getAvatar());
            group.put("items", itemViews);
            result.add(group);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItem updateItem(String userId, String itemId, Integer quantity, String spec, String remark) {
        CartItem item = getOwnedItem(userId, itemId);
        if (item == null) {
            return null;
        }

        if (quantity != null) {
            if (quantity <= 0) {
                // 数量改成 0 等价于删除，避免购物车里留下 0 份的幽灵条目
                deleteItem(userId, itemId);
                return null;
            }
            item.setQuantity(Math.min(quantity, MAX_QUANTITY));
        }

        if (spec != null) {
            String newSpec = spec.trim();
            if (!newSpec.equals(item.getSpec())) {
                // 改规格后可能与另一条已有项撞键：并入那条，再删掉当前这条
                CartItem sameSpec = findByUniqueKey(userId, item.getMerchantId(), item.getDishId(), newSpec);
                if (sameSpec != null && !sameSpec.getId().equals(item.getId())) {
                    sameSpec.setQuantity(Math.min(sameSpec.getQuantity() + item.getQuantity(), MAX_QUANTITY));
                    sameSpec.setUpdateTime(LocalDateTime.now());
                    updateById(sameSpec);
                    removeById(item.getId());
                    return sameSpec;
                }
            }
            item.setSpec(newSpec);
        }

        if (remark != null) {
            item.setRemark(remark.trim());
        }

        item.setUpdateTime(LocalDateTime.now());
        if (!updateById(item)) {
            throw new BusinessException("500", "更新购物车失败，请稍后重试");
        }
        return item;
    }

    @Override
    public boolean deleteItem(String userId, String itemId) {
        CartItem item = getOwnedItem(userId, itemId);
        if (item == null) {
            return false;
        }
        return removeById(item.getId());
    }

    @Override
    public int deleteByDish(String userId, String merchantId, String dishId) {
        if (merchantId == null || merchantId.isBlank() || dishId == null || dishId.isBlank()) {
            return 0;
        }
        List<CartItem> matches = list(new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, userId)
            .eq(CartItem::getMerchantId, merchantId)
            .eq(CartItem::getDishId, dishId));
        if (matches.isEmpty()) {
            return 0;
        }
        return removeByIds(matches.stream().map(CartItem::getId).toList()) ? matches.size() : 0;
    }

    @Override
    public int batchDelete(String userId, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return 0;
        }
        List<Long> ids = new ArrayList<>();
        for (String raw : itemIds) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(raw.trim()));
            } catch (NumberFormatException e) {
                // 非法 ID 直接忽略：批量删除不该因为一个脏值整批失败
                log.debug("忽略非法购物车项ID: {}", raw);
            }
        }
        if (ids.isEmpty()) {
            return 0;
        }
        // 只删属于该用户的条目，天然挡住跨用户批量删除
        List<CartItem> owned = list(new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, userId)
            .in(CartItem::getId, ids));
        if (owned.isEmpty()) {
            return 0;
        }
        return removeByIds(owned.stream().map(CartItem::getId).toList()) ? owned.size() : 0;
    }

    @Override
    public int clear(String userId, String merchantId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, userId);
        if (merchantId != null && !merchantId.isBlank()) {
            wrapper.eq(CartItem::getMerchantId, merchantId);
        }
        long count = count(wrapper);
        if (count == 0) {
            return 0;
        }
        return remove(wrapper) ? (int) count : 0;
    }

    @Override
    public boolean selectItem(String userId, String itemId, Boolean selected) {
        CartItem item = getOwnedItem(userId, itemId);
        if (item == null) {
            return false;
        }
        return update(new LambdaUpdateWrapper<CartItem>()
            .eq(CartItem::getId, item.getId())
            .set(CartItem::getSelected, selected == null || selected)
            .set(CartItem::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public int selectAll(String userId, String merchantId, Boolean selected) {
        LambdaUpdateWrapper<CartItem> wrapper = new LambdaUpdateWrapper<CartItem>()
            .eq(CartItem::getUserId, userId)
            .set(CartItem::getSelected, selected == null || selected)
            .set(CartItem::getUpdateTime, LocalDateTime.now());
        if (merchantId != null && !merchantId.isBlank()) {
            wrapper.eq(CartItem::getMerchantId, merchantId);
        }
        long count = count(new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, userId)
            .eq(merchantId != null && !merchantId.isBlank(), CartItem::getMerchantId, merchantId));
        if (count == 0) {
            return 0;
        }
        return update(wrapper) ? (int) count : 0;
    }

    /** 按唯一键查已有项（userId + merchantId + dishId + spec）。 */
    private CartItem findByUniqueKey(String userId, String merchantId, String dishId, String spec) {
        return getOne(new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, userId)
            .eq(CartItem::getMerchantId, merchantId)
            .eq(CartItem::getDishId, dishId)
            .eq(CartItem::getSpec, spec == null ? "" : spec), false);
    }

    /** 取属于该用户的购物车项，不属于就当作不存在，避免越权读到他人条目。 */
    private CartItem getOwnedItem(String userId, String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        Long id;
        try {
            id = Long.parseLong(itemId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        CartItem item = getById(id);
        if (item == null || !userId.equals(item.getUserId())) {
            return null;
        }
        return item;
    }

    /** 简单的 JSON 字符串数组序列化，避免为一个字段引入额外依赖。 */
    private String toJsonArray(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            String value = values.get(i) == null ? "" : values.get(i);
            sb.append('"').append(value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        return sb.append(']').toString();
    }

    /** 解析 JSON 字符串数组；解析不了就返回空列表，不抛异常。 */
    private List<String> parseJsonArray(String json) {
        List<String> values = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return values;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return values;
        }
        String body = trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return values;
        }
        for (String part : body.split(",")) {
            String value = part.trim();
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
            }
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }
}
