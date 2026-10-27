package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.*;
import com.xx.jaseatschoicejava.entity.*;
import com.xx.jaseatschoicejava.enums.AddDishApprovalStatus;
import com.xx.jaseatschoicejava.enums.AddDishPermission;
import com.xx.jaseatschoicejava.mapper.AddDishRequestMapper;
import com.xx.jaseatschoicejava.mapper.AddDishSettingMapper;
import com.xx.jaseatschoicejava.mapper.DishFeatureMapper;
import com.xx.jaseatschoicejava.service.AddDishService;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.GroupMemberService;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.service.IngredientConflictRuleService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 加菜服务实现
 */
@Slf4j
@Service
public class AddDishServiceImpl extends ServiceImpl<AddDishRequestMapper, AddDishRequest> implements AddDishService {

    @Autowired
    private AddDishSettingMapper settingMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DishService dishService;

    @Autowired
    private GroupOrderService groupOrderService;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private UserService userService;

    @Autowired
    private IngredientConflictRuleService conflictRuleService;

    @Autowired
    private DishFeatureMapper dishFeatureMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAddDishRequest(CreateAddDishDTO dto, String requestUserId) {
        // 1. 检查权限
        if (!checkAddDishPermission(dto.getGroupOrderId(), requestUserId)) {
            throw new RuntimeException("无加菜权限");
        }

        // 2. 获取群订单信息
        GroupOrder groupOrder = groupOrderService.getById(dto.getGroupOrderId());
        if (groupOrder == null) {
            throw new RuntimeException("群订单不存在");
        }

        // 3. 检查加菜设置（预算限制、数量限制）
        BigDecimal totalAmount = calculateTotalAmount(dto);
        AddDishSetting setting = getSettingEntity(dto.getGroupOrderId());
        if (setting != null) {
            // 检查数量限制
            if (setting.getMaxDishCount() != null &&
                dto.getDishItems().size() > setting.getMaxDishCount()) {
                throw new RuntimeException("超过单次加菜数量限制");
            }

            if (setting.getBudgetLimit() != null &&
                totalAmount.compareTo(setting.getBudgetLimit()) > 0) {
                throw new RuntimeException("超过单次加菜预算限制");
            }
        }

        // 4. 创建加菜请求
        AddDishRequest request = new AddDishRequest();
        // 使用IdGenerator生成ID
        String requestId = IdGenerator.toAddDishRequestIdString(IdGenerator.generateId());
        request.setId(requestId);
        request.setGroupOrderId(dto.getGroupOrderId());
        request.setOriginalOrderId(dto.getOriginalOrderId());
        request.setRequestUserId(requestUserId);
        request.setMerchantId(groupOrder.getMerchantId());
        request.setTotalAmount(totalAmount);
        request.setApprovalStatus(AddDishApprovalStatus.PENDING.getValue());
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        // 设置超时时间
        request.setTimeoutTime(LocalDateTime.now().plusMinutes(15));
        request.setFirstRemindTime(LocalDateTime.now().plusMinutes(10));

        // 保存菜品信息为JSON
        try {
            request.setDishInfo(objectMapper.writeValueAsString(dto.getDishItems()));
        } catch (JsonProcessingException e) {
            log.error("序列化菜品信息失败", e);
            throw new RuntimeException("保存菜品信息失败");
        }

        // 5. 保存到数据库
        save(request);

        // 6. 发送WebSocket通知（需要在后续集成）
        // notifyAddDishRequest(request);

        return request.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultDTO batchReview(BatchReviewDTO dto) {
        List<String> failedReasons = new ArrayList<>();
        int approvedCount = 0;
        int rejectedCount = 0;

        for (String requestId : dto.getRequestIds()) {
            try {
                AddDishRequest request = getById(requestId);
                if (request == null) {
                    failedReasons.add(requestId + ": 请求不存在");
                    continue;
                }

                // 检查状态
                if (!AddDishApprovalStatus.PENDING.getValue().equals(request.getApprovalStatus())) {
                    failedReasons.add(requestId + ": 该请求已审核");
                    continue;
                }

                // 更新状态
                if ("approve".equals(dto.getAction())) {
                    request.setApprovalStatus(AddDishApprovalStatus.APPROVED.getValue());
                    approvedCount++;

                    // 审核通过后创建订单（需要调用OrderService）
                    // Order addOrder = createAddOrder(request);
                } else if ("reject".equals(dto.getAction())) {
                    request.setApprovalStatus(AddDishApprovalStatus.REJECTED.getValue());
                    request.setRejectReason(dto.getRejectReason());
                    rejectedCount++;
                }

                request.setReviewerId(dto.getReviewerId());
                request.setReviewTime(LocalDateTime.now());
                request.setUpdatedAt(LocalDateTime.now());
                updateById(request);

            } catch (Exception e) {
                log.error("审核请求失败, requestId: {}", requestId, e);
                failedReasons.add(requestId + ": " + e.getMessage());
            }
        }

        if (!failedReasons.isEmpty()) {
            return ReviewResultDTO.failure(failedReasons);
        }

        return ReviewResultDTO.success(approvedCount, rejectedCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawRequest(String requestId, String userId) {
        AddDishRequest request = getById(requestId);
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }

        // 只有请求人可以撤回
        if (!request.getRequestUserId().equals(userId)) {
            throw new RuntimeException("无权撤回此请求");
        }

        // 只能撤回待审核状态的请求
        if (!AddDishApprovalStatus.PENDING.getValue().equals(request.getApprovalStatus())) {
            throw new RuntimeException("该请求已审核，无法撤回");
        }

        request.setApprovalStatus(AddDishApprovalStatus.WITHDRAWN.getValue());
        request.setUpdatedAt(LocalDateTime.now());

        return updateById(request);
    }

    @Override
    public List<AddDishRequestVO> getReviewList(String groupOrderId) {
        LambdaQueryWrapper<AddDishRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddDishRequest::getGroupOrderId, groupOrderId)
                   .eq(AddDishRequest::getApprovalStatus, AddDishApprovalStatus.PENDING.getValue())
                   .orderByDesc(AddDishRequest::getCreatedAt);

        List<AddDishRequest> requests = list(queryWrapper);
        return requests.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<AddDishRequestVO> getHistory(String groupOrderId) {
        LambdaQueryWrapper<AddDishRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddDishRequest::getGroupOrderId, groupOrderId)
                   .in(AddDishRequest::getApprovalStatus,
                       AddDishApprovalStatus.APPROVED.getValue(),
                       AddDishApprovalStatus.REJECTED.getValue(),
                       AddDishApprovalStatus.WITHDRAWN.getValue(),
                       AddDishApprovalStatus.TIMEOUT_REJECTED.getValue())
                   .orderByDesc(AddDishRequest::getCreatedAt);

        List<AddDishRequest> requests = list(queryWrapper);
        return requests.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public AllergyCheckResultDTO checkAllergyConflict(CreateAddDishDTO dto) {
        if (dto == null || dto.getGroupOrderId() == null) {
            return new AllergyCheckResultDTO(false, new ArrayList<>());
        }

        GroupOrder groupOrder = groupOrderService.getById(dto.getGroupOrderId());
        if (groupOrder == null || groupOrder.getGroupId() == null) {
            return new AllergyCheckResultDTO(false, new ArrayList<>());
        }

        Map<String, User> groupUsers = new HashMap<>();
        List<GroupMember> groupMembers = groupMemberService.getGroupMembers(groupOrder.getGroupId());
        if (groupMembers != null) {
            for (GroupMember member : groupMembers) {
                User user = userService.getById(member.getUserId());
                if (user != null) {
                    groupUsers.put(user.getUserId(), user);
                }
            }
        }

        List<IngredientConflictRule> rules = conflictRuleService.getEnabledRules();
        if (rules == null) {
            rules = new ArrayList<>();
        }

        List<AllergyCheckResultDTO.ConflictItem> conflicts = new ArrayList<>();
        if (dto.getDishItems() != null) {
            for (CreateAddDishDTO.DishItem item : dto.getDishItems()) {
                Dish dish = dishService.getById(item.getDishId());
                if (dish == null) {
                    continue;
                }

                Set<String> ingredientNames = resolveIngredientNames(dish);
                List<AllergyCheckResultDTO.UserConflict> conflictUsers = new ArrayList<>();
                for (User user : groupUsers.values()) {
                    List<String> matchedAllergies = new ArrayList<>();
                    Set<String> userAllergies = new LinkedHashSet<>(parseAllergies(user.getAllergies()));
                    userAllergies.addAll(parseAllergies(user.getPreferTags()));
                    for (String allergy : userAllergies) {
                        if (matchesDishIngredient(allergy, ingredientNames, rules)) {
                            matchedAllergies.add(allergy);
                        }
                    }
                    if (!matchedAllergies.isEmpty()) {
                        conflictUsers.add(new AllergyCheckResultDTO.UserConflict(
                                user.getUserId(), user.getNickname(), matchedAllergies));
                    }
                }

                if (!conflictUsers.isEmpty()) {
                    conflicts.add(new AllergyCheckResultDTO.ConflictItem(
                            dish.getId(), dish.getName(), conflictUsers));
                }
            }
        }

        return new AllergyCheckResultDTO(!conflicts.isEmpty(), conflicts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<AddDishRequest> timeoutRequests = baseMapper.selectTimeoutRequests(now);

        for (AddDishRequest request : timeoutRequests) {
            try {
                request.setApprovalStatus(AddDishApprovalStatus.TIMEOUT_REJECTED.getValue());
                request.setUpdatedAt(LocalDateTime.now());
                updateById(request);

                // 发送超时通知
                // notifyTimeout(request);
                log.info("加菜请求超时自动驳回, requestId: {}", request.getId());
            } catch (Exception e) {
                log.error("处理超时请求失败, requestId: {}", request.getId(), e);
            }
        }
    }

    @Override
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<AddDishRequest> needRemind = baseMapper.selectNeedRemindRequests(now);

        for (AddDishRequest request : needRemind) {
            try {
                request.setSecondRemindTime(now);
                updateById(request);

                // 发送提醒通知
                // notifyReminder(request);
                log.info("发送加菜超时提醒, requestId: {}", request.getId());
            } catch (Exception e) {
                log.error("发送提醒失败, requestId: {}", request.getId(), e);
            }
        }
    }

    @Override
    public boolean checkAddDishPermission(String groupOrderId, String userId) {
        // 1. 检查群订单是否允许加菜
        // 2. 获取加菜设置
        AddDishSetting setting = getSettingEntity(groupOrderId);
        if (setting == null) {
            // 默认全员可加菜
            return true;
        }

        // 3. 检查权限模式
        if (AddDishPermission.INITIATOR_ONLY.getValue().equals(setting.getAddDishPermission())) {
            // 仅发起者可加菜 - 需要检查是否为发起者
            GroupOrder groupOrder = groupOrderService.getById(groupOrderId);
            return groupOrder != null && Objects.equals(groupOrder.getInitiatorId(), userId);
        }

        return true;
    }

    @Override
    public AddDishSettingDTO getSetting(String groupOrderId) {
        AddDishSetting setting = getSettingEntity(groupOrderId);
        AddDishSettingDTO dto = new AddDishSettingDTO();
        if (setting != null) {
            dto.setGroupOrderId(setting.getGroupOrderId());
            dto.setAddDishPermission(setting.getAddDishPermission());
            dto.setBudgetLimit(setting.getBudgetLimit());
            dto.setMaxDishCount(setting.getMaxDishCount());
        } else {
            // 返回默认设置
            dto.setGroupOrderId(groupOrderId);
            dto.setAddDishPermission(AddDishPermission.ALL_MEMBERS.getValue());
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSetting(AddDishSettingDTO dto) {
        AddDishSetting setting = new AddDishSetting();
        setting.setGroupOrderId(dto.getGroupOrderId());
        setting.setAddDishPermission(dto.getAddDishPermission());
        setting.setBudgetLimit(dto.getBudgetLimit());
        setting.setMaxDishCount(dto.getMaxDishCount());

        LambdaQueryWrapper<AddDishSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddDishSetting::getGroupOrderId, dto.getGroupOrderId());

        AddDishSetting existing = settingMapper.selectOne(queryWrapper);
        if (existing != null) {
            setting.setId(existing.getId());
            return settingMapper.updateById(setting) > 0;
        } else {
            return settingMapper.insert(setting) > 0;
        }
    }

    /**
     * 获取加菜设置实体
     */
    private AddDishSetting getSettingEntity(String groupOrderId) {
        LambdaQueryWrapper<AddDishSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddDishSetting::getGroupOrderId, groupOrderId);
        return settingMapper.selectOne(queryWrapper);
    }

    /**
     * 按菜品价格和数量计算总金额
     */
    private BigDecimal calculateTotalAmount(CreateAddDishDTO dto) {
        if (dto == null || dto.getDishItems() == null || dto.getDishItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CreateAddDishDTO.DishItem item : dto.getDishItems()) {
            Dish dish = dishService.getById(item.getDishId());
            if (dish == null || dish.getPrice() == null) {
                throw new RuntimeException("菜品不存在或未设置价格: " + item.getDishId());
            }
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        return total;
    }

    /**
     * 获取菜品的食材名称，优先使用菜品特征中的名称，并解析菜品 JSON 中的 ID。
     */
    private Set<String> resolveIngredientNames(Dish dish) {
        Set<String> tokens = new LinkedHashSet<>();
        tokens.addAll(parseIngredientTokens(dish.getIngredients()));

        tokens.addAll(parseIngredientTokens(
                dishFeatureMapper.getIngredientsJsonByDishId(dish.getId())));

        Set<String> names = new LinkedHashSet<>();
        for (String token : tokens) {
            if (!isNumeric(token)) {
                names.add(token);
            }
        }
        return names;
    }

    private Set<String> parseIngredientTokens(String ingredientsJson) {
        Set<String> tokens = new LinkedHashSet<>();
        if (ingredientsJson == null || ingredientsJson.trim().isEmpty()) {
            return tokens;
        }
        try {
            collectIngredientTokens(objectMapper.readTree(ingredientsJson), tokens);
        } catch (Exception e) {
            log.warn("解析菜品食材失败: {}", ingredientsJson, e);
        }
        return tokens;
    }

    private void collectIngredientTokens(JsonNode node, Set<String> tokens) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isValueNode()) {
            String value = node.asText().trim();
            if (!value.isEmpty()) {
                tokens.add(value);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectIngredientTokens(child, tokens);
            }
            return;
        }
        if (node.isObject()) {
            JsonNode name = node.get("name");
            if (name == null) {
                name = node.get("ingredientName");
            }
            if (name != null && !name.isNull()) {
                tokens.add(name.asText());
                return;
            }

            JsonNode id = node.get("id");
            if (id == null) {
                id = node.get("ingredientId");
            }
            if (id != null && !id.isNull()) {
                tokens.add(id.asText());
                return;
            }

            node.fields().forEachRemaining(entry -> collectIngredientTokens(entry.getValue(), tokens));
        }
    }

    private List<String> parseAllergies(JsonNode allergies) {
        List<String> result = new ArrayList<>();
        if (allergies == null || allergies.isNull()) {
            return result;
        }
        if (allergies.isArray()) {
            for (JsonNode node : allergies) {
                if (node != null && !node.isNull() && !node.asText().trim().isEmpty()) {
                    result.add(node.asText().trim());
                }
            }
        } else if (allergies.isObject()) {
            JsonNode nested = allergies.get("allergies");
            if (nested != null && nested.isArray()) {
                for (JsonNode node : nested) {
                    if (node != null && !node.isNull() && !node.asText().trim().isEmpty()) {
                        result.add(node.asText().trim());
                    }
                }
            }
        } else if (allergies.isTextual()) {
            for (String item : allergies.asText().split("[,，、]")) {
                if (!item.trim().isEmpty()) {
                    result.add(item.trim());
                }
            }
        }
        return result;
    }

    private boolean matchesDishIngredient(String allergy, Set<String> ingredientNames,
                                          List<IngredientConflictRule> rules) {
        for (String ingredientName : ingredientNames) {
            if (ingredientNameMatches(allergy, ingredientName)) {
                return true;
            }
        }

        for (IngredientConflictRule rule : rules) {
            for (String ruleIngredient : parseRuleIngredients(rule.getMainIngredients())) {
                boolean dishContains = ingredientNames.stream()
                        .anyMatch(name -> ingredientNameMatches(name, ruleIngredient));
                if (dishContains && ingredientNameMatches(allergy, ruleIngredient)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> parseRuleIngredients(String json) {
        List<String> result = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return result;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.isArray()) {
                for (JsonNode item : node) {
                    if (item != null && !item.isNull() && !item.asText().trim().isEmpty()) {
                        result.add(item.asText().trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析冲突规则食材失败: {}", json, e);
        }
        return result;
    }

    private boolean ingredientNameMatches(String left, String right) {
        String normalizedLeft = normalizeIngredientName(left);
        String normalizedRight = normalizeIngredientName(right);
        if (normalizedLeft.isEmpty() || normalizedRight.isEmpty()) {
            return false;
        }
        return normalizedLeft.equals(normalizedRight)
                || normalizedLeft.contains(normalizedRight)
                || normalizedRight.contains(normalizedLeft);
    }

    private String normalizeIngredientName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replaceAll("\\s+", "")
                .replaceAll("[（(].*?[）)]", "")
                .toLowerCase(Locale.ROOT);
    }

    private boolean isNumeric(String value) {
        return value != null && value.matches("\\d+");
    }

    /**
     * 转换为VO
     */
    private AddDishRequestVO convertToVO(AddDishRequest request) {
        AddDishRequestVO vo = new AddDishRequestVO();
        vo.setId(request.getId());
        vo.setGroupOrderId(request.getGroupOrderId());
        vo.setStatus(request.getApprovalStatus());
        vo.setStatusDesc(AddDishApprovalStatus.fromValue(request.getApprovalStatus()).getDesc());
        vo.setSubmitTime(request.getCreatedAt());
        vo.setRejectReason(request.getRejectReason());
        vo.setTotalAmount(request.getTotalAmount());

        // 计算剩余时间
        if (request.getTimeoutTime() != null &&
            AddDishApprovalStatus.PENDING.getValue().equals(request.getApprovalStatus())) {
            long remaining = java.time.Duration.between(LocalDateTime.now(), request.getTimeoutTime()).getSeconds();
            vo.setRemainingTime(Math.max(0, remaining));
        }

        // 解析菜品信息
        try {
            List<CreateAddDishDTO.DishItem> items = objectMapper.readValue(
                request.getDishInfo(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CreateAddDishDTO.DishItem.class)
            );
            // TODO: 转换为DishInfo并填充菜品名称、价格等
        } catch (JsonProcessingException e) {
            log.error("解析菜品信息失败", e);
        }

        return vo;
    }
}
