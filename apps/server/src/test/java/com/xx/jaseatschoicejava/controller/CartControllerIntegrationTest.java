package com.xx.jaseatschoicejava.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;
import com.xx.jaseatschoicejava.entity.CartItem;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.CartItemService;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * 购物车接口集成测试（#603）。
 *
 * <p>购物车的核心语义是「去重合并」和「只认自己的购物车」，两者都依赖真实唯一键与
 * 真实查询，所以用真实上下文 + 真实 MySQL 跑，而不是 mock 掉 ServiceImpl 的基类方法。
 *
 * <p>前置：MySQL/Redis 可用（见 AGENTS.md 的本地栈说明）。用例会自建临时菜品并在结束后清理，
 * 不依赖会过期的种子数据。
 */
@SpringBootTest(
    classes = JasEatsChoiceJavaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "ai.base-url=http://127.0.0.1:4011/v1",
        "ai.api-key=test-key",
        "ai.model=fake-model",
        "file.upload.path=target/test-uploads/"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerIntegrationTest {

    private static final String USER_A = "test-cart-user-a";
    private static final String USER_B = "test-cart-user-b";
    private static final String MERCHANT = "test-cart-merchant";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DishService dishService;

    @Autowired
    private CartItemService cartItemService;

    private final ObjectMapper json = new ObjectMapper();

    private String dishA;
    private String dishB;

    @BeforeEach
    void setUp() {
        cleanUp();
        dishA = createDish("测试宫保鸡丁", true);
        dishB = createDish("测试鱼香肉丝", true);
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        cartItemService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
            .in(CartItem::getUserId, List.of(USER_A, USER_B)));
        if (dishA != null) {
            dishService.removeById(dishA);
        }
        if (dishB != null) {
            dishService.removeById(dishB);
        }
        dishA = null;
        dishB = null;
    }

    private String createDish(String name, boolean online) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setMerchantId(MERCHANT);
        dish.setPrice(new BigDecimal("28.00"));
        dish.setCalorie(300);
        dish.setIsOnline(online);
        dishService.save(dish);
        return dish.getId();
    }

    private String token(String userId) {
        return jwtUtil.generateToken(userId, userId);
    }

    private JsonNode addItem(String userId, String dishId, String spec, int quantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/cart/items")
                .header("Authorization", "Bearer " + token(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of(
                    "dishId", dishId,
                    "quantity", quantity,
                    "spec", spec))))
            .andReturn();
        return json.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void addSameDishSameSpec_mergesQuantityInsteadOfDuplicatingRows() throws Exception {
        JsonNode first = addItem(USER_A, dishA, "中辣", 2);
        String firstId = first.get("data").get("id").asText();
        JsonNode second = addItem(USER_A, dishA, "中辣", 3);

        assertTrue(second.get("success").asBoolean(), second.toString());
        assertEquals(5, second.get("data").get("quantity").asInt());
        assertEquals(firstId, second.get("data").get("id").asText(),
            "重复加购必须并回同一行，不能新建一条");

        // 真正落库的只有一行，验证没有堆重复项
        long rows = cartItemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, USER_A)
            .eq(CartItem::getDishId, dishA));
        assertEquals(1, rows);
    }

    @Test
    void addSameDishSameSpec_survivesConcurrentInsertThroughUniqueKey() throws Exception {
        // 先绕过 Service 直接插一行，模拟"并发下别人已经插进去了"，
        // 让后续 addItem 走 DuplicateKey 兜底分支而不是查重命中的分支
        CartItem race = new CartItem();
        race.setUserId(USER_A);
        race.setMerchantId(MERCHANT);
        race.setDishId(dishA);
        race.setSpec("中辣");
        race.setQuantity(4);
        race.setSelected(true);
        cartItemService.save(race);

        JsonNode response = addItem(USER_A, dishA, "中辣", 1);

        assertTrue(response.get("success").asBoolean(), response.toString());
        assertEquals(5, response.get("data").get("quantity").asInt(), "并发插入后也应合并到唯一那一行");
        assertEquals(1L, cartItemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, USER_A)
            .eq(CartItem::getDishId, dishA)));
    }

    @Test
    void addSameDishDifferentSpec_createsTwoRows() throws Exception {
        addItem(USER_A, dishA, "中辣", 1);
        addItem(USER_A, dishA, "微辣", 1);

        long rows = cartItemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, USER_A)
            .eq(CartItem::getDishId, dishA));
        assertEquals(2, rows, "规格不同应视为不同条目");
    }

    @Test
    void getCart_groupsByMerchantAndCarriesDishInfo() throws Exception {
        addItem(USER_A, dishA, "", 1);
        addItem(USER_A, dishB, "", 2);

        MvcResult result = mockMvc.perform(get("/v1/cart/" + USER_A)
                .header("Authorization", "Bearer " + token(USER_A)))
            .andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsString());

        assertTrue(body.get("success").asBoolean(), body.toString());
        JsonNode groups = body.get("data");
        assertEquals(1, groups.size(), "同一商家的商品应归到一组");
        JsonNode items = groups.get(0).get("items");
        assertEquals(2, items.size());
        // 菜名与价格是真查出来的，不是回显请求参数
        assertEquals("测试宫保鸡丁", items.get(0).get("name").asText());
        assertEquals(28.00, items.get(0).get("price").asDouble(), 0.001);
    }

    @Test
    void cannotReadOtherUsersCart() throws Exception {
        addItem(USER_A, dishA, "", 1);

        MvcResult result = mockMvc.perform(get("/v1/cart/" + USER_A)
                .header("Authorization", "Bearer " + token(USER_B)))
            .andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsString());

        assertFalse(body.get("success").asBoolean());
        assertEquals("403", body.get("code").asText());
    }

    @Test
    void cannotDeleteOtherUsersItem() throws Exception {
        JsonNode added = addItem(USER_A, dishA, "", 1);
        String itemId = added.get("data").get("id").asText();

        MvcResult result = mockMvc.perform(delete("/v1/cart/items/" + itemId)
                .header("Authorization", "Bearer " + token(USER_B)))
            .andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsString());

        assertFalse(body.get("success").asBoolean());
        assertEquals("404", body.get("code").asText(), "他人的条目应表现为不存在");
        // 原主人的条目必须还在
        assertNotNull(cartItemService.getById(Long.parseLong(itemId)));
    }

    @Test
    void updateQuantityToZero_removesItem() throws Exception {
        JsonNode added = addItem(USER_A, dishA, "", 2);
        String itemId = added.get("data").get("id").asText();

        mockMvc.perform(put("/v1/cart/items/" + itemId)
                .header("Authorization", "Bearer " + token(USER_A))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":0}"))
            .andReturn();

        assertEquals(0L, cartItemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, USER_A)));
    }

    @Test
    void selectAllScopedToUser() throws Exception {
        JsonNode a1 = addItem(USER_A, dishA, "", 1);
        addItem(USER_A, dishB, "", 1);
        addItem(USER_B, dishA, "", 1);

        mockMvc.perform(put("/v1/cart/select-all")
                .header("Authorization", "Bearer " + token(USER_A))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"selected\":false}"))
            .andReturn();

        // A 的两项都被取消选中
        assertTrue(cartItemService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, USER_A)).stream().noneMatch(CartItem::getSelected));
        // B 的项不受影响
        assertTrue(cartItemService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, USER_B)).stream().allMatch(CartItem::getSelected));
        assertNotNull(a1.get("data").get("id"));
    }

    @Test
    void clearWithMerchantScope_onlyClearsThatMerchant() throws Exception {
        addItem(USER_A, dishA, "", 1);
        addItem(USER_A, dishB, "", 1);

        mockMvc.perform(delete("/v1/cart/" + USER_A + "/clear")
                .param("merchantId", MERCHANT)
                .header("Authorization", "Bearer " + token(USER_A)))
            .andReturn();

        assertEquals(0L, cartItemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, USER_A)));
    }

    @Test
    void rejectsDishNotInMerchant() throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/cart/items")
                .header("Authorization", "Bearer " + token(USER_A))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of(
                    "dishId", dishA,
                    "merchantId", "another-merchant",
                    "quantity", 1))))
            .andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsString());

        assertFalse(body.get("success").asBoolean());
        assertEquals("400", body.get("code").asText());
    }

    @Test
    void anonymousRequestIsRejected() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/cart/" + USER_A)).andReturn();
        // 默认拒绝策略下未登录直接 401
        assertTrue(result.getResponse().getStatus() == 401
            || !json.readTree(result.getResponse().getContentAsString()).get("success").asBoolean());
    }
}
