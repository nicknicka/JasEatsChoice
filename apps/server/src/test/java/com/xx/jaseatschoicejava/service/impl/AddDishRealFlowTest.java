package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.AddDishSettingDTO;
import com.xx.jaseatschoicejava.dto.CreateAddDishDTO;
import com.xx.jaseatschoicejava.entity.AddDishRequest;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.GroupMember;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.IngredientConflictRule;
import com.xx.jaseatschoicejava.entity.JFoodNutrition;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.enums.AddDishPermission;
import com.xx.jaseatschoicejava.mapper.AddDishRequestMapper;
import com.xx.jaseatschoicejava.mapper.AddDishSettingMapper;
import com.xx.jaseatschoicejava.mapper.DishFeatureMapper;
import com.xx.jaseatschoicejava.mapper.JFoodNutritionMapper;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.GroupMemberService;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.service.IngredientConflictRuleService;
import com.xx.jaseatschoicejava.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddDishRealFlowTest {

    @Mock
    private AddDishRequestMapper requestMapper;

    @Mock
    private AddDishSettingMapper settingMapper;

    @Mock
    private DishService dishService;

    @Mock
    private GroupOrderService groupOrderService;

    @Mock
    private GroupMemberService groupMemberService;

    @Mock
    private UserService userService;

    @Mock
    private IngredientConflictRuleService conflictRuleService;

    @Mock
    private DishFeatureMapper dishFeatureMapper;

    @Mock
    private JFoodNutritionMapper foodNutritionMapper;

    private AddDishServiceImpl addDishService;

    @Before
    public void setUp() {
        addDishService = newAddDishService();
    }

    @Test
    public void addDish_whenAllergyMatches_shouldReportConflict() {
        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setId("group-order-1");
        groupOrder.setGroupId("group-1");
        when(groupOrderService.getById("group-order-1")).thenReturn(groupOrder);
        when(dishService.getById("dish-1")).thenReturn(dish("dish-1", "花生鸡丁", "[]"));
        when(dishFeatureMapper.getIngredientsJsonByDishId("dish-1")).thenReturn("[\"花生\", \"鸡肉\"]");
        GroupMember member = new GroupMember();
        member.setUserId("user-1");
        when(groupMemberService.getGroupMembers("group-1")).thenReturn(List.of(member));
        User user = new User();
        user.setUserId("user-1");
        user.setNickname("小明");
        user.setPreferTags(new ObjectMapper().createObjectNode()
                .putArray("allergies").add("花生"));
        when(userService.getById("user-1")).thenReturn(user);
        when(conflictRuleService.getEnabledRules()).thenReturn(List.of(rule("花生")));

        var result = addDishService.checkAllergyConflict(new CreateAddDishDTO("group-order-1", null,
                List.of(new CreateAddDishDTO.DishItem("dish-1", 1, null))));

        assertTrue(result.getHasConflict());
        assertEquals("花生", result.getConflicts().get(0).getConflictUsers().get(0).getAllergies().get(0));
    }

    @Test
    public void addDish_whenNoAllergyMatches_shouldPass() {
        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setId("group-order-1");
        groupOrder.setGroupId("group-1");
        when(groupOrderService.getById("group-order-1")).thenReturn(groupOrder);
        when(dishService.getById("dish-1")).thenReturn(dish("dish-1", "青菜", "[\"青菜\"]"));
        GroupMember member = new GroupMember();
        member.setUserId("user-1");
        when(groupMemberService.getGroupMembers("group-1")).thenReturn(List.of(member));
        User user = new User();
        user.setUserId("user-1");
        user.setNickname("小明");
        user.setAllergies(new ObjectMapper().createArrayNode().add("花生"));
        when(userService.getById("user-1")).thenReturn(user);
        when(conflictRuleService.getEnabledRules()).thenReturn(List.of(rule("花生")));

        var result = addDishService.checkAllergyConflict(new CreateAddDishDTO("group-order-1", null,
                List.of(new CreateAddDishDTO.DishItem("dish-1", 1, null))));

        assertFalse(result.getHasConflict());
        assertTrue(result.getConflicts().isEmpty());
    }

    @Test
    public void permission_whenInitiatorOnly_shouldRejectOtherMember() {
        var setting = new com.xx.jaseatschoicejava.entity.AddDishSetting();
        setting.setGroupOrderId("group-order-1");
        setting.setAddDishPermission(AddDishPermission.INITIATOR_ONLY.getValue());
        when(settingMapper.selectOne(any())).thenReturn(setting);
        GroupOrder order = new GroupOrder();
        order.setInitiatorId("initiator");
        when(groupOrderService.getById("group-order-1")).thenReturn(order);

        assertFalse(addDishService.checkAddDishPermission("group-order-1", "member"));
        assertTrue(addDishService.checkAddDishPermission("group-order-1", "initiator"));
    }

    @Test
    public void createRequest_whenInitiatorOnlyAndOtherMember_shouldReject() {
        var setting = new com.xx.jaseatschoicejava.entity.AddDishSetting();
        setting.setGroupOrderId("group-order-1");
        setting.setAddDishPermission(AddDishPermission.INITIATOR_ONLY.getValue());
        when(settingMapper.selectOne(any())).thenReturn(setting);
        GroupOrder order = new GroupOrder();
        order.setInitiatorId("initiator");
        when(groupOrderService.getById("group-order-1")).thenReturn(order);

        CreateAddDishDTO dto = new CreateAddDishDTO("group-order-1", null,
                List.of(new CreateAddDishDTO.DishItem("dish-1", 1, null)));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> addDishService.createAddDishRequest(dto, "member"));
        assertEquals("无加菜权限", error.getMessage());
    }

    @Test
    public void createRequest_shouldCalculateTotalAmountFromDishPrices() {
        when(groupOrderService.getById("group-order-1")).thenReturn(new GroupOrder());
        when(dishService.getById("dish-1")).thenReturn(dish("dish-1", "菜1", "[]"));
        when(dishService.getById("dish-2")).thenReturn(dish("dish-2", "菜2", "[]"));

        CreateAddDishDTO dto = new CreateAddDishDTO("group-order-1", null,
                List.of(new CreateAddDishDTO.DishItem("dish-1", 2, null),
                        new CreateAddDishDTO.DishItem("dish-2", 1, null)));
        addDishService.createAddDishRequest(dto, "user-1");

        ArgumentCaptor<AddDishRequest> captor = ArgumentCaptor.forClass(AddDishRequest.class);
        verify(requestMapper).insert(captor.capture());
        assertEquals(new BigDecimal("57.00"), captor.getValue().getTotalAmount());
    }

    @Test
    public void ingredientRule_whenNutritionIdMapsToName_shouldMatch() {
        IngredientConflictRuleServiceImpl service = spy(new IngredientConflictRuleServiceImpl());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "foodNutritionMapper", foodNutritionMapper);
        IngredientConflictRule rule = rule("花生");
        doReturn(List.of(rule)).when(service).getEnabledRules();
        JFoodNutrition food = new JFoodNutrition();
        food.setId(1L);
        food.setFoodName("花生(烤，勤俭牌)");
        when(foodNutritionMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(food));

        var conflicts = service.checkConflicts(List.of(1L));

        assertEquals(1, conflicts.size());
        assertEquals("[\"花生\"]", conflicts.get(0).getMainIngredients());
    }

    @Test
    public void groupOrderAddition_shouldCalculateTotalAmountFromDishPrices() {
        DishService dishService = org.mockito.Mockito.mock(DishService.class);
        when(dishService.getById("dish-1")).thenReturn(dish("dish-1", "菜1", "[]"));
        when(dishService.getById("dish-2")).thenReturn(dish("dish-2", "菜2", "[]"));
        GroupOrderAdditionServiceImpl service = new GroupOrderAdditionServiceImpl(
                null, dishService, null, null, null, null, null, new ObjectMapper());

        BigDecimal total = service.calculateTotalAmount(List.of(
                Map.of("dishId", "dish-1", "quantity", 2),
                Map.of("dishId", "dish-2", "quantity", 1)));

        assertEquals(new BigDecimal("57.00"), total);
    }

    private AddDishServiceImpl newAddDishService() {
        AddDishServiceImpl service = new AddDishServiceImpl();
        ReflectionTestUtils.setField(service, "settingMapper", settingMapper);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "dishService", dishService);
        ReflectionTestUtils.setField(service, "groupOrderService", groupOrderService);
        ReflectionTestUtils.setField(service, "groupMemberService", groupMemberService);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "conflictRuleService", conflictRuleService);
        ReflectionTestUtils.setField(service, "dishFeatureMapper", dishFeatureMapper);
        ReflectionTestUtils.setField(service, "baseMapper", requestMapper);
        return service;
    }

    private Dish dish(String id, String name, String ingredients) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName(name);
        dish.setIngredients(ingredients);
        dish.setPrice(new BigDecimal("19.00"));
        return dish;
    }

    private IngredientConflictRule rule(String ingredient) {
        IngredientConflictRule rule = new IngredientConflictRule();
        rule.setMainIngredients("[\"" + ingredient + "\"]");
        rule.setSeverity(3);
        return rule;
    }
}
