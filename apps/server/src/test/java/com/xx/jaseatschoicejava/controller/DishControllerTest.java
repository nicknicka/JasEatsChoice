package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MenuService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DishControllerTest {

    @Test
    void updateDishStatus_persistsIsOnline() {
        DishService dishService = mock(DishService.class);
        DishController controller = new DishController();
        ReflectionTestUtils.setField(controller, "dishService", dishService);
        ReflectionTestUtils.setField(controller, "menuService", mock(MenuService.class));

        Dish dish = new Dish();
        dish.setId("dish-1");
        dish.setIsOnline(true);
        Dish persistedDish = new Dish();
        persistedDish.setId("dish-1");
        persistedDish.setIsOnline(false);
        when(dishService.getById("dish-1")).thenReturn(dish, persistedDish);
        when(dishService.updateById(any(Dish.class))).thenReturn(true);

        ResponseResult<?> response = controller.updateDishStatus("dish-1", Map.of("status", false));

        assertTrue(response.getSuccess());
        ArgumentCaptor<Dish> captor = ArgumentCaptor.forClass(Dish.class);
        verify(dishService).updateById(captor.capture());
        assertFalse(captor.getValue().getIsOnline());
        assertEquals("dish-1", captor.getValue().getId());
    }

    @Test
    void updateDishStatus_whenUpdateFails_returnsFailure() {
        DishService dishService = mock(DishService.class);
        DishController controller = new DishController();
        ReflectionTestUtils.setField(controller, "dishService", dishService);
        ReflectionTestUtils.setField(controller, "menuService", mock(MenuService.class));

        Dish dish = new Dish();
        dish.setId("dish-1");
        when(dishService.getById("dish-1")).thenReturn(dish);
        when(dishService.updateById(any(Dish.class))).thenReturn(false);

        ResponseResult<?> response = controller.updateDishStatus("dish-1", Map.of("status", false));

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
    }

    @Test
    void updateDishStatus_whenValueNotPersisted_returnsFailure() {
        DishService dishService = mock(DishService.class);
        DishController controller = new DishController();
        ReflectionTestUtils.setField(controller, "dishService", dishService);
        ReflectionTestUtils.setField(controller, "menuService", mock(MenuService.class));

        Dish dish = new Dish();
        dish.setId("dish-1");
        dish.setIsOnline(true);
        when(dishService.getById("dish-1")).thenReturn(dish);
        when(dishService.updateById(any(Dish.class))).thenReturn(true);

        ResponseResult<?> response = controller.updateDishStatus("dish-1", Map.of("status", false));

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
    }

    @Test
    void updateDishStatus_whenStatusMissing_doesNotUpdate() {
        DishService dishService = mock(DishService.class);
        DishController controller = new DishController();
        ReflectionTestUtils.setField(controller, "dishService", dishService);

        ResponseResult<?> response = controller.updateDishStatus("dish-1", Map.of());

        assertFalse(response.getSuccess());
        assertEquals("400", response.getCode());
        assertNull(response.getData());
        verify(dishService, never()).updateById(any(Dish.class));
    }
}
