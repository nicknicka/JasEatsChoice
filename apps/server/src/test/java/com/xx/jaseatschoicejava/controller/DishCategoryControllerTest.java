package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DishCategoryControllerTest {

    @Test
    public void returnsDistinctNonBlankCategories() {
        DishController controller = new DishController();
        mockDishService(controller, "热菜", "汤羹", "热菜", "  ");

        ResponseResult<?> response = controller.getDishCategories("merchant-1");

        assertTrue(response.getSuccess());
        assertEquals(List.of("热菜", "汤羹"), response.getData());
    }

    @Test
    public void categoriesPathIsNotCapturedByDishIdMapping() throws Exception {
        DishController controller = new DishController();
        mockDishService(controller, "热菜", "汤羹");

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/v1/dishes/categories").param("merchantId", "merchant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("热菜"))
                .andExpect(jsonPath("$.data[1]").value("汤羹"));
    }

    private DishService mockDishService(DishController controller, String... categories) {
        DishService dishService = mock(DishService.class);
        ReflectionTestUtils.setField(controller, "dishService", dishService);
        List<Dish> dishes = Arrays.stream(categories).map(category -> {
            Dish dish = new Dish();
            dish.setCategory(category);
            return dish;
        }).toList();
        when(dishService.list(org.mockito.ArgumentMatchers.<Wrapper<Dish>>any())).thenReturn(dishes);
        return dishService;
    }
}
