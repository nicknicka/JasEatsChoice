package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xx.jaseatschoicejava.entity.ContentExtraction;
import com.xx.jaseatschoicejava.entity.ContentSource;
import com.xx.jaseatschoicejava.entity.Recipe;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Spy
    @InjectMocks
    private RecipeServiceImpl service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createFromExtraction_shouldMapFieldsAndDefaultNutritionToZero() throws Exception {
        ContentSource source = new ContentSource();
        source.setUserId("user-1");
        source.setContentUrl("https://www.xiaohongshu.com/explore/abc");
        source.setPlatform("XIAOHONGSHU");

        ContentExtraction extraction = new ContentExtraction();
        extraction.setDishName("番茄炒蛋");
        extraction.setDescription("酸甜开胃");
        extraction.setIngredients("[{\"name\":\"番茄\",\"amount\":\"2个\"},{\"name\":\"鸡蛋\",\"amount\":\"3个\"}]");
        extraction.setSteps("[{\"stepNumber\":1,\"title\":\"切配\",\"description\":\"番茄切块，鸡蛋打散\"},{\"stepNumber\":2,\"description\":\"热锅炒熟\"}]");
        extraction.setCookingTime(15);
        extraction.setCalories(180);
        doAnswer(invocation -> invocation.getArgument(0)).when(service).addRecipe(any());

        Recipe recipe = service.createFromExtraction(extraction, source, "dinner");

        assertEquals("user-1", recipe.getUserId());
        assertEquals("番茄炒蛋", recipe.getName());
        assertEquals("dinner", recipe.getType());
        assertEquals(180, recipe.getCalories());
        assertEquals(0, recipe.getProtein());
        assertEquals(0, recipe.getCarbs());
        assertEquals(0, recipe.getFat());
        assertEquals("15分钟", recipe.getCookTime());
        assertEquals(source.getContentUrl(), recipe.getSourceUrl());
        assertEquals("XIAOHONGSHU", recipe.getSourcePlatform());
        assertEquals("酸甜开胃\n\n制作步骤：\n1. 切配：番茄切块，鸡蛋打散\n2. 热锅炒熟", recipe.getDetail());

        Map<String, Object> dish = objectMapper.readValue(recipe.getItems(),
            new TypeReference<java.util.List<Map<String, Object>>>() {}).get(0);
        assertEquals("番茄炒蛋", dish.get("name"));
        assertEquals(java.util.List.of("番茄 2个", "鸡蛋 3个"), dish.get("ingredients"));
        assertEquals(180, dish.get("calories"));
        verify(service).addRecipe(recipe);
    }

    @Test
    void createFromExtraction_withoutMealType_shouldUseLunch() {
        ContentSource source = new ContentSource();
        source.setUserId("user-1");
        ContentExtraction extraction = new ContentExtraction();
        extraction.setDishName("测试菜");
        doAnswer(invocation -> invocation.getArgument(0)).when(service).addRecipe(any());

        Recipe recipe = service.createFromExtraction(extraction, source, null);

        assertEquals("lunch", recipe.getType());
    }

    @Test
    void createFromExtraction_withInvalidMealType_shouldReject() {
        ContentSource source = new ContentSource();
        ContentExtraction extraction = new ContentExtraction();

        assertThrows(IllegalArgumentException.class,
            () -> service.createFromExtraction(extraction, source, "夜宵"));
    }
}
