package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;
import com.xx.jaseatschoicejava.entity.GroupOrderMember;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.GroupOrderDishMapper;
import com.xx.jaseatschoicejava.mapper.GroupOrderMapper;
import com.xx.jaseatschoicejava.mapper.GroupOrderMemberMapper;
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

    private static final String ROLE_INITIATOR = "initiator";
    private static final String ROLE_MEMBER = "member";
    private static final String PAY_STATUS_PENDING = "pending";
    private static final String PAY_STATUS_PAID = "paid";
    private static final String GROUP_ORDER_CONFIRMED_PREFIX = "[GROUP_ORDER_CONFIRMED]";

    @Autowired
    private GroupOrderDishMapper groupOrderDishMapper;

    @Autowired
    private GroupOrderMemberMapper groupOrderMemberMapper;

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
        LocalDateTime now = LocalDateTime.now();
        groupOrder.setCreateTime(now);
        groupOrder.setUpdateTime(now);
        if (groupOrder.getStatus() == null) {
            groupOrder.setStatus(0);
        }
        if (groupOrder.getLocked() == null) {
            groupOrder.setLocked(false);
        }
        if (groupOrder.getMaxParticipants() == null || groupOrder.getMaxParticipants() <= 0) {
            groupOrder.setMaxParticipants(1);
        }
        if (!Boolean.TRUE.equals(groupOrder.getLocked())) {
            groupOrder.setConfirmedTime(null);
        }

        boolean orderSaved = save(groupOrder);
        if (!orderSaved) {
            return false;
        }

        if (groupOrder.getInitiatorId() != null && !groupOrder.getInitiatorId().isBlank()) {
            ensureGroupOrderMember(groupOrder.getId(), groupOrder.getInitiatorId(), ROLE_INITIATOR, null);
        }

        if (dishItems != null) {
            for (GroupOrderDish dish : dishItems) {
                dish.setGroupOrderId(groupOrder.getId());
                if (dish.getUserId() == null || dish.getUserId().isBlank()) {
                    dish.setUserId(groupOrder.getInitiatorId());
                }
                if (groupOrderDishMapper.insert(dish) <= 0) {
                    return false;
                }
                if (dish.getUserId() != null && !dish.getUserId().isBlank()) {
                    String role = Objects.equals(dish.getUserId(), groupOrder.getInitiatorId())
                            ? ROLE_INITIATOR
                            : ROLE_MEMBER;
                    ensureGroupOrderMember(groupOrder.getId(), dish.getUserId(), role, groupOrder.getInitiatorId());
                }
            }
        }

        refreshGroupOrderAggregate(groupOrder.getId());
        return true;
    }

    @Override
    public List<GroupOrder> getGroupOrdersByGroupId(String groupId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<GroupOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrder::getGroupId, groupId);
        queryWrapper.orderByDesc(GroupOrder::getCreateTime);

        if (status != null) {
            queryWrapper.eq(GroupOrder::getStatus, status);
        }

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
        queryWrapper.orderByAsc(GroupOrderDish::getId);
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

        LambdaQueryWrapper<GroupOrderMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(GroupOrderMember::getUserId, userId)
                .eq(GroupOrderMember::getLeaveStatus, 0);
        List<GroupOrderMember> joinedMembers = groupOrderMemberMapper.selectList(memberQuery);
        Set<String> joinedOrderIds = new LinkedHashSet<>();
        joinedMembers.forEach(item -> joinedOrderIds.add(item.getGroupOrderId()));

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
                .sorted(Comparator.comparing(
                        GroupOrder::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .skip((long) Math.max(page - 1, 0) * size)
                .limit(size)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> joinByOrderCode(String orderCode, String userId) {
        GroupOrder targetOrder = list().stream()
                .filter(order -> Objects.equals(buildOrderCode(order.getId()), orderCode))
                .findFirst()
                .orElse(null);

        if (targetOrder == null) {
            return null;
        }

        boolean locked = isOrderLocked(targetOrder);
        boolean joined = isActiveGroupOrderMember(targetOrder.getId(), userId);
        Integer status = targetOrder.getStatus() == null ? 0 : targetOrder.getStatus();
        int currentCount = Math.max(getActiveGroupOrderMembers(targetOrder.getId()).size(), 1);
        int maxParticipants = targetOrder.getMaxParticipants() == null || targetOrder.getMaxParticipants() <= 0
                ? currentCount
                : Math.max(targetOrder.getMaxParticipants(), currentCount);
        boolean joinable = status <= 0 && !locked && (joined || currentCount < maxParticipants);

        if (!joined && joinable) {
            ensureGroupOrderMember(targetOrder.getId(), userId, ROLE_MEMBER, targetOrder.getInitiatorId());
            refreshGroupOrderAggregate(targetOrder.getId());
            joined = true;
            targetOrder = getById(targetOrder.getId());
            currentCount = Math.max(getActiveGroupOrderMembers(targetOrder.getId()).size(), 1);
            maxParticipants = targetOrder.getMaxParticipants() == null || targetOrder.getMaxParticipants() <= 0
                    ? currentCount
                    : Math.max(targetOrder.getMaxParticipants(), currentCount);
            joinable = status <= 0 && !locked && currentCount < maxParticipants;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groupOrderId", targetOrder.getId());
        result.put("orderId", targetOrder.getId());
        result.put("orderCode", buildOrderCode(targetOrder.getId()));
        result.put("groupId", targetOrder.getGroupId());
        result.put("merchantId", targetOrder.getMerchantId());
        result.put("status", targetOrder.getStatus());
        result.put("locked", isOrderLocked(targetOrder));
        result.put("currentCount", currentCount);
        result.put("maxParticipants", maxParticipants);
        result.put("joined", joined);
        result.put("joinable", joinable);
        result.put("message", joinable || joined ? "订单码有效，可进入拼单详情" : "当前拼单人数已满，无法加入");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> saveUserSelections(String groupOrderId, String userId, List<Map<String, Object>> dishes) {
        GroupOrder groupOrder = getById(groupOrderId);
        boolean alreadyMember = isActiveGroupOrderMember(groupOrderId, userId);
        if (!alreadyMember && groupOrder != null) {
            Map<String, Object> runtimeState = getGroupOrderRuntimeState(groupOrder, userId);
            int currentCount = ((Number) runtimeState.getOrDefault("currentCount", 0)).intValue();
            int maxParticipants = ((Number) runtimeState.getOrDefault("maxParticipants", Math.max(currentCount, 1))).intValue();
            if (maxParticipants > 0 && currentCount >= maxParticipants) {
                throw new IllegalStateException("当前拼单人数已满，无法保存选菜");
            }
        }

        String role = groupOrder != null && Objects.equals(groupOrder.getInitiatorId(), userId)
                ? ROLE_INITIATOR
                : ROLE_MEMBER;
        ensureGroupOrderMember(groupOrderId, userId, role, groupOrder == null ? null : groupOrder.getInitiatorId());

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

        refreshGroupOrderAggregate(groupOrderId);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean leaveGroupOrder(String groupOrderId, String userId) {
        GroupOrderMember member = findGroupOrderMember(groupOrderId, userId, true);
        if (member == null) {
            return false;
        }

        member.setLeaveStatus(1);
        member.setLeaveTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        groupOrderMemberMapper.updateById(member);

        LambdaQueryWrapper<GroupOrderDish> removeQuery = new LambdaQueryWrapper<>();
        removeQuery.eq(GroupOrderDish::getGroupOrderId, groupOrderId)
                .eq(GroupOrderDish::getUserId, userId);
        groupOrderDishMapper.delete(removeQuery);

        refreshGroupOrderAggregate(groupOrderId);
        return true;
    }

    @Override
    public List<GroupOrderMember> getActiveGroupOrderMembers(String groupOrderId) {
        LambdaQueryWrapper<GroupOrderMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderMember::getGroupOrderId, groupOrderId)
                .eq(GroupOrderMember::getLeaveStatus, 0)
                .orderByAsc(GroupOrderMember::getJoinTime)
                .orderByAsc(GroupOrderMember::getId);
        return groupOrderMemberMapper.selectList(queryWrapper);
    }

    @Override
    public boolean isActiveGroupOrderMember(String groupOrderId, String userId) {
        return findGroupOrderMember(groupOrderId, userId, true) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupOrderMember ensureGroupOrderMember(String groupOrderId, String userId, String role, String inviteBy) {
        if (groupOrderId == null || groupOrderId.isBlank() || userId == null || userId.isBlank()) {
            return null;
        }

        GroupOrderMember existing = findGroupOrderMember(groupOrderId, userId, false);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setRole((role == null || role.isBlank()) ? existing.getRole() : role);
            existing.setInviteBy(inviteBy == null || inviteBy.isBlank() ? existing.getInviteBy() : inviteBy);
            if (existing.getLeaveStatus() != null && existing.getLeaveStatus() == 1) {
                existing.setLeaveStatus(0);
                existing.setLeaveTime(null);
                existing.setJoinTime(now);
                existing.setPayStatus(PAY_STATUS_PENDING);
                existing.setPaidAmount(BigDecimal.ZERO);
            }
            existing.setUpdateTime(now);
            groupOrderMemberMapper.updateById(existing);
            return existing;
        }

        GroupOrderMember member = new GroupOrderMember();
        member.setGroupOrderId(groupOrderId);
        member.setUserId(userId);
        member.setRole(role == null || role.isBlank() ? ROLE_MEMBER : role);
        member.setJoinTime(now);
        member.setPayStatus(PAY_STATUS_PENDING);
        member.setPaidAmount(BigDecimal.ZERO);
        member.setLeaveStatus(0);
        member.setLeaveTime(null);
        member.setInviteBy(inviteBy);
        member.setCreateTime(now);
        member.setUpdateTime(now);
        groupOrderMemberMapper.insert(member);
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncGroupOrderMemberPaymentStatus(String groupOrderId, String userId, BigDecimal paidAmount, boolean paid) {
        GroupOrder groupOrder = getById(groupOrderId);
        String role = groupOrder != null && Objects.equals(groupOrder.getInitiatorId(), userId)
                ? ROLE_INITIATOR
                : ROLE_MEMBER;
        GroupOrderMember member = ensureGroupOrderMember(groupOrderId, userId, role, null);
        if (member == null) {
            return;
        }

        member.setPayStatus(paid ? PAY_STATUS_PAID : PAY_STATUS_PENDING);
        if (paidAmount != null) {
            member.setPaidAmount(paidAmount);
        } else if (member.getPaidAmount() == null) {
            member.setPaidAmount(BigDecimal.ZERO);
        }
        member.setUpdateTime(LocalDateTime.now());
        groupOrderMemberMapper.updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncAllGroupOrderMembersPaid(String groupOrderId) {
        List<GroupOrderMember> members = getActiveGroupOrderMembers(groupOrderId);
        LocalDateTime now = LocalDateTime.now();
        for (GroupOrderMember member : members) {
            member.setPayStatus(PAY_STATUS_PAID);
            if (member.getPaidAmount() == null) {
                member.setPaidAmount(BigDecimal.ZERO);
            }
            member.setUpdateTime(now);
            groupOrderMemberMapper.updateById(member);
        }
    }

    @Override
    public Map<String, Object> getGroupOrderRuntimeState(GroupOrder groupOrder, String currentUserId) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (groupOrder == null) {
            return result;
        }

        List<GroupOrderMember> members = getActiveGroupOrderMembers(groupOrder.getId());
        LinkedHashMap<String, BigDecimal> userAmountMap = buildUserOrderAmountMap(getGroupOrderDishes(groupOrder.getId()));
        Set<String> payableParticipantUserIds = getPayableParticipantUserIds(userAmountMap);
        int currentCount = Math.max(members.size(), groupOrder.getInitiatorId() == null || groupOrder.getInitiatorId().isBlank() ? 0 : 1);
        int normalizedCurrentCount = Math.max(currentCount, 1);
        int maxParticipants = groupOrder.getMaxParticipants() == null || groupOrder.getMaxParticipants() <= 0
                ? normalizedCurrentCount
                : Math.max(groupOrder.getMaxParticipants(), normalizedCurrentCount);
        boolean locked = isOrderLocked(groupOrder);
        boolean currentUserJoined = currentUserId != null
                && !currentUserId.isBlank()
                && members.stream().anyMatch(item -> currentUserId.equals(item.getUserId()));
        BigDecimal currentUserAmount = currentUserId == null
                ? BigDecimal.ZERO
                : userAmountMap.getOrDefault(currentUserId, BigDecimal.ZERO);
        boolean currentUserPaid = currentUserJoined && isMemberPaid(findGroupOrderMember(groupOrder.getId(), currentUserId, true), currentUserAmount);
        boolean paidByAll = payableParticipantUserIds.isEmpty() || payableParticipantUserIds.stream().allMatch(memberUserId -> {
            GroupOrderMember member = findGroupOrderMember(groupOrder.getId(), memberUserId, true);
            return member != null && PAY_STATUS_PAID.equals(member.getPayStatus());
        });

        List<Map<String, Object>> participants = members.stream()
                .map(member -> buildParticipantPayload(member, userAmountMap))
                .toList();

        result.put("members", participants);
        result.put("participants", participants);
        result.put("currentCount", normalizedCurrentCount);
        result.put("maxParticipants", maxParticipants);
        result.put("locked", locked);
        result.put("paidByAll", paidByAll);
        result.put("confirmedTime", groupOrder.getConfirmedTime());
        result.put("joinable", groupOrder.getStatus() != null
                && groupOrder.getStatus() <= 0
                && !locked
                && normalizedCurrentCount < maxParticipants);
        result.put("currentUserJoined", currentUserJoined);
        result.put("currentUserPaid", currentUserPaid);
        result.put("canEdit", currentUserJoined
                && groupOrder.getStatus() != null
                && groupOrder.getStatus() <= 0
                && !locked);
        result.put("canLeave", currentUserJoined
                && currentUserId != null
                && !currentUserId.equals(groupOrder.getInitiatorId())
                && groupOrder.getStatus() != null
                && groupOrder.getStatus() <= 0
                && !locked
                && !currentUserPaid);
        result.put("canConfirm", currentUserId != null
                && currentUserId.equals(groupOrder.getInitiatorId())
                && groupOrder.getStatus() != null
                && groupOrder.getStatus() == 0
                && !locked
                && !getGroupOrderDishes(groupOrder.getId()).isEmpty());
        result.put("canPay", currentUserJoined
                && groupOrder.getStatus() != null
                && groupOrder.getStatus() == 0
                && locked
                && currentUserAmount.compareTo(BigDecimal.ZERO) > 0
                && !currentUserPaid);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupOrder confirmGroupOrder(String groupOrderId) {
        GroupOrder groupOrder = getById(groupOrderId);
        if (groupOrder == null) {
            return null;
        }

        groupOrder.setLocked(true);
        if (groupOrder.getConfirmedTime() == null) {
            groupOrder.setConfirmedTime(LocalDateTime.now());
        }
        if (groupOrder.getMaxParticipants() == null || groupOrder.getMaxParticipants() <= 0) {
            groupOrder.setMaxParticipants(Math.max(getActiveGroupOrderMembers(groupOrderId).size(), 1));
        }
        groupOrder.setTotalAmount(calculateGroupOrderTotalAmount(groupOrderId).doubleValue());
        groupOrder.setUpdateTime(LocalDateTime.now());
        updateById(groupOrder);
        return getById(groupOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupOrder refreshGroupOrderPaymentState(String groupOrderId, String userId, BigDecimal paidAmount, String paymentType) {
        GroupOrder groupOrder = getById(groupOrderId);
        if (groupOrder == null) {
            return null;
        }

        LinkedHashMap<String, BigDecimal> userAmountMap = buildUserOrderAmountMap(getGroupOrderDishes(groupOrderId));
        if ("all".equals(paymentType)) {
            for (GroupOrderMember member : getActiveGroupOrderMembers(groupOrderId)) {
                syncGroupOrderMemberPaymentStatus(
                        groupOrderId,
                        member.getUserId(),
                        userAmountMap.getOrDefault(member.getUserId(), BigDecimal.ZERO),
                        true
                );
            }
        } else if (userId != null && !userId.isBlank()) {
            BigDecimal normalizedPaidAmount = paidAmount != null
                    ? paidAmount
                    : userAmountMap.getOrDefault(userId, BigDecimal.ZERO);
            syncGroupOrderMemberPaymentStatus(groupOrderId, userId, normalizedPaidAmount, true);
        }

        Set<String> payableParticipantUserIds = getPayableParticipantUserIds(userAmountMap);
        boolean fullyPaid = payableParticipantUserIds.isEmpty() || payableParticipantUserIds.stream().allMatch(memberUserId -> {
            GroupOrderMember member = findGroupOrderMember(groupOrderId, memberUserId, true);
            return member != null && PAY_STATUS_PAID.equals(member.getPayStatus());
        });

        groupOrder.setLocked(Boolean.TRUE.equals(groupOrder.getLocked()) || isOrderLocked(groupOrder));
        groupOrder.setTotalAmount(calculateGroupOrderTotalAmount(groupOrderId).doubleValue());
        if (fullyPaid) {
            if (groupOrder.getStatus() == null || groupOrder.getStatus() <= 0) {
                groupOrder.setStatus(1);
            }
            groupOrder.setLocked(true);
            if (groupOrder.getConfirmedTime() == null) {
                groupOrder.setConfirmedTime(LocalDateTime.now());
            }
        }
        groupOrder.setUpdateTime(LocalDateTime.now());
        updateById(groupOrder);
        return getById(groupOrderId);
    }

    private GroupOrderMember findGroupOrderMember(String groupOrderId, String userId, boolean activeOnly) {
        if (groupOrderId == null || groupOrderId.isBlank() || userId == null || userId.isBlank()) {
            return null;
        }

        LambdaQueryWrapper<GroupOrderMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderMember::getGroupOrderId, groupOrderId)
                .eq(GroupOrderMember::getUserId, userId);
        if (activeOnly) {
            queryWrapper.eq(GroupOrderMember::getLeaveStatus, 0);
        }
        queryWrapper.orderByDesc(GroupOrderMember::getLeaveStatus)
                .orderByDesc(GroupOrderMember::getUpdateTime)
                .last("LIMIT 1");
        return groupOrderMemberMapper.selectOne(queryWrapper);
    }

    private boolean isMemberPaid(GroupOrderMember member, BigDecimal orderAmount) {
        if (member == null) {
            return false;
        }
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return PAY_STATUS_PAID.equals(member.getPayStatus());
    }

    private void refreshGroupOrderAggregate(String groupOrderId) {
        GroupOrder groupOrder = getById(groupOrderId);
        if (groupOrder == null) {
            return;
        }

        int currentCount = Math.max(getActiveGroupOrderMembers(groupOrderId).size(),
                groupOrder.getInitiatorId() == null || groupOrder.getInitiatorId().isBlank() ? 0 : 1);
        int normalizedCurrentCount = Math.max(currentCount, 1);

        if (groupOrder.getMaxParticipants() == null || groupOrder.getMaxParticipants() <= 0) {
            groupOrder.setMaxParticipants(normalizedCurrentCount);
        } else if (groupOrder.getMaxParticipants() < normalizedCurrentCount) {
            groupOrder.setMaxParticipants(normalizedCurrentCount);
        }

        groupOrder.setTotalAmount(calculateGroupOrderTotalAmount(groupOrderId).doubleValue());
        groupOrder.setUpdateTime(LocalDateTime.now());
        updateById(groupOrder);
    }

    private boolean isOrderLocked(GroupOrder groupOrder) {
        if (groupOrder == null) {
            return false;
        }
        if (Boolean.TRUE.equals(groupOrder.getLocked())) {
            return true;
        }
        if (groupOrder.getStatus() != null && groupOrder.getStatus() > 0) {
            return true;
        }
        return groupOrder.getRemark() != null && groupOrder.getRemark().startsWith(GROUP_ORDER_CONFIRMED_PREFIX);
    }

    private BigDecimal calculateGroupOrderTotalAmount(String groupOrderId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (GroupOrderDish dishItem : getGroupOrderDishes(groupOrderId)) {
            Dish dish = dishService.getById(dishItem.getDishId());
            if (dish == null || dish.getPrice() == null || dishItem.getQuantity() == null) {
                continue;
            }
            totalAmount = totalAmount.add(dish.getPrice().multiply(BigDecimal.valueOf(dishItem.getQuantity())));
        }
        return totalAmount;
    }

    private LinkedHashMap<String, BigDecimal> buildUserOrderAmountMap(List<GroupOrderDish> dishItems) {
        LinkedHashMap<String, BigDecimal> userOrderAmountMap = new LinkedHashMap<>();
        for (GroupOrderDish dishItem : dishItems) {
            if (dishItem.getUserId() == null || dishItem.getUserId().isBlank()) {
                continue;
            }

            Dish dish = dishService.getById(dishItem.getDishId());
            if (dish == null || dish.getPrice() == null || dishItem.getQuantity() == null) {
                userOrderAmountMap.putIfAbsent(dishItem.getUserId(), BigDecimal.ZERO);
                continue;
            }

            BigDecimal lineAmount = dish.getPrice().multiply(BigDecimal.valueOf(dishItem.getQuantity()));
            userOrderAmountMap.merge(dishItem.getUserId(), lineAmount, BigDecimal::add);
        }
        return userOrderAmountMap;
    }

    private Set<String> getPayableParticipantUserIds(LinkedHashMap<String, BigDecimal> userOrderAmountMap) {
        Set<String> payableParticipantUserIds = new LinkedHashSet<>();
        for (Map.Entry<String, BigDecimal> entry : userOrderAmountMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                payableParticipantUserIds.add(entry.getKey());
            }
        }
        return payableParticipantUserIds;
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

    private Map<String, Object> buildParticipantPayload(GroupOrderMember member,
                                                        LinkedHashMap<String, BigDecimal> userAmountMap) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (member == null) {
            return payload;
        }

        User user = userService.getById(member.getUserId());
        BigDecimal amount = userAmountMap.getOrDefault(member.getUserId(), BigDecimal.ZERO);
        boolean paid = isMemberPaid(member, amount);

        payload.put("id", member.getId());
        payload.put("userId", member.getUserId());
        payload.put("role", member.getRole());
        payload.put("payStatus", member.getPayStatus());
        payload.put("paidAmount", member.getPaidAmount() == null ? BigDecimal.ZERO : member.getPaidAmount());
        payload.put("amount", amount);
        payload.put("paid", paid);
        payload.put("joinTime", member.getJoinTime());
        payload.put("name", user == null || user.getNickname() == null ? "" : user.getNickname());
        payload.put("nickname", user == null || user.getNickname() == null ? "" : user.getNickname());
        payload.put("userName", user == null || user.getNickname() == null ? "" : user.getNickname());
        payload.put("avatar", user == null || user.getAvatar() == null ? "" : user.getAvatar());
        return payload;
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
