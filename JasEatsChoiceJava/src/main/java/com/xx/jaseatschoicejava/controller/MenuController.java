package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.entity.MenuDish;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MenuService;
import com.xx.jaseatschoicejava.service.MenuDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import com.xx.jaseatschoicejava.dto.MenuWithDishStatusDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuDishService menuDishService;

    @Autowired
    private DishService dishService;

    /**
     * 根据商家ID获取菜单列表
     */
    @GetMapping("/merchants/{merchantId}/menu")
    public ResponseResult<?> getMenusByMerchantId(@PathVariable String merchantId) {
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Menu::getMerchantId, merchantId);
        // 获取所有菜单，不区分状态
        List<Menu> menus = menuService.list(queryWrapper);

        // 将后端的状态转换为前端需要的格式
        menus.forEach(menu -> {
            if ("active".equals(menu.getStatus())) {
                menu.setStatus("online");
            } else {
                // 将inactive统一转换为offline（前端没有inactive状态）
                menu.setStatus("offline");
            }
        });

        // 转换为包含菜品详细信息的Map列表
        List<Map<String, Object>> resultMenus = menus.stream().map(menu -> {
            Map<String, Object> menuMap = new java.util.HashMap<>();
            try {
                java.beans.BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(Menu.class);
                java.beans.PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (java.beans.PropertyDescriptor pd : propertyDescriptors) {
                    if (!"class".equals(pd.getName())) {
                        Object value = pd.getReadMethod().invoke(menu);
                        // 处理字段名映射，将 type 转换为 category（前端使用的字段名）
                        String fieldName = pd.getName();
                        if ("type".equals(fieldName)) {
                            menuMap.put("category", value);
                        } else if ("name".equals(fieldName)) {
                            menuMap.put("menuName", value); // 前端期望 menuName
                        } else {
                            menuMap.put(fieldName, value);
                        }
                    }
                }
                // 获取菜单关联的菜品详细信息
                LambdaQueryWrapper<MenuDish> menuDishQueryWrapper = new LambdaQueryWrapper<>();
                menuDishQueryWrapper.eq(MenuDish::getMenuId, menu.getId());
                List<MenuDish> menuDishes = menuDishService.list(menuDishQueryWrapper);

                // 根据菜品ID查询完整的菜品信息
                List<Map<String, Object>> dishes = menuDishes.stream()
                        .map(menuDish -> dishService.getById(menuDish.getDishId()))
                        .filter(dishObj -> dishObj != null) // 过滤掉可能已删除的菜品
                        .map(dishObj -> {
                            Dish dish = (Dish) dishObj; // 显式转换
                            Map<String, Object> dishMap = new java.util.HashMap<>();
                            dishMap.put("id", dish.getId());
                            dishMap.put("name", dish.getName());
                            dishMap.put("price", dish.getPrice());
                            dishMap.put("status", dish.getStatus());
                            dishMap.put("category", dish.getCategory()); // 添加菜品分类
                            dishMap.put("description", dish.getDescription()); // 添加描述
                            dishMap.put("calorie", dish.getCalorie()); // 添加卡路里

                            // 解析食材数据
                            Map<String, Object> ingredientsData = parseIngredients(dish.getIngredients());
                            dishMap.put("optionalIngredients", ingredientsData.get("optionalIngredients"));
                            dishMap.put("requiredIngredients", ingredientsData.get("requiredIngredients"));

                            return dishMap;
                        })
                        .collect(java.util.stream.Collectors.toList());

                menuMap.put("dishes", dishes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return menuMap;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseResult.success(resultMenus);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{menuId}")
    public ResponseResult<?> getMenuDetail(@PathVariable String menuId) {
        Menu menu = menuService.getById(menuId);
        if (menu != null) {
            // 将后端的状态转换为前端需要的格式
            if ("active".equals(menu.getStatus())) {
                menu.setStatus("online");
            } else {
                menu.setStatus("offline");
            }
            // 添加菜品数量字段
            Integer dishCount = menuService.getDishCountByMenuId(menuId);
            // 转换为Map，添加菜品数量字段
            Map<String, Object> menuMap = new java.util.HashMap<>();
            try {
                java.beans.BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(Menu.class);
                java.beans.PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (java.beans.PropertyDescriptor pd : propertyDescriptors) {
                    if (!"class".equals(pd.getName())) {
                        Object value = pd.getReadMethod().invoke(menu);
                        // 处理字段名映射，与@JsonProperty注解保持一致
                        String fieldName = pd.getName();
                        if ("type".equals(fieldName)) {
                            menuMap.put("category", value);
                        } else if ("autoStartTime".equals(fieldName)) {
                            menuMap.put("autoOnline", value);
                        } else if ("autoEndTime".equals(fieldName)) {
                            menuMap.put("autoOffline", value);
                        } else {
                            menuMap.put(fieldName, value);
                        }
                    }
                }
                menuMap.put("dishes", dishCount);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseResult.fail("500", "获取菜单详情失败");
            }
            return ResponseResult.success(menuMap);
        }
        return ResponseResult.fail("404", "菜单不存在");
    }

    /**
     * 获取菜单详情（根据商家ID和菜单ID）
     */
    @GetMapping("/merchants/{merchantId}/menu/{menuId}")
    public ResponseResult<?> getMenuDetailByMerchant(@PathVariable String merchantId, @PathVariable String menuId) {
        Menu menu = menuService.getById(menuId);
        // 验证菜单是否属于该商家
        if (menu != null && merchantId.equals(menu.getMerchantId())) {
            // 将后端的状态转换为前端需要的格式
            if ("active".equals(menu.getStatus())) {
                menu.setStatus("online");
            } else {
                menu.setStatus("offline");
            }
            // 添加菜品数量字段
            Integer dishCount = menuService.getDishCountByMenuId(menuId);
            // 转换为Map，添加菜品数量字段
            Map<String, Object> menuMap = new java.util.HashMap<>();
            try {
                java.beans.BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(Menu.class);
                java.beans.PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (java.beans.PropertyDescriptor pd : propertyDescriptors) {
                    if (!"class".equals(pd.getName())) {
                        Object value = pd.getReadMethod().invoke(menu);
                        // 处理字段名映射，与@JsonProperty注解保持一致
                        String fieldName = pd.getName();
                        if ("type".equals(fieldName)) {
                            menuMap.put("category", value);
                        } else if ("autoStartTime".equals(fieldName)) {
                            menuMap.put("autoOnline", value);
                        } else if ("autoEndTime".equals(fieldName)) {
                            menuMap.put("autoOffline", value);
                        } else {
                            menuMap.put(fieldName, value);
                        }
                    }
                }
                menuMap.put("dishes", dishCount);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseResult.fail("500", "获取菜单详情失败");
            }
            return ResponseResult.success(menuMap);
        }
        return ResponseResult.fail("404", "菜单不存在");
    }

    /**
     * 获取菜单关联的菜品列表（返回所有状态的菜品）
     */
    @GetMapping("/merchants/{merchantId}/menu/{menuId}/dishes")
    public ResponseResult<?> getMenuDishes(@PathVariable String merchantId, @PathVariable String menuId) {
        // 首先验证菜单是否属于该商家
        Menu menu = menuService.getById(menuId);
        if (menu == null || !merchantId.equals(menu.getMerchantId())) {
            return ResponseResult.fail("404", "菜单不存在");
        }

        // 查询该菜单下的所有菜品关联（包括上架和下架状态）
        LambdaQueryWrapper<MenuDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuDish::getMenuId, menuId);
        List<MenuDish> menuDishes = menuDishService.list(queryWrapper);

        // 根据菜品ID查询完整的菜品信息，并合并菜单内状态
        List<Map<String, Object>> dishes = menuDishes.stream()
                .map(menuDish -> {
                    Dish dish = dishService.getById(menuDish.getDishId());
                    if (dish == null) {
                        return null;
                    }
                    // 转换为 Map，方便添加菜单内状态信息
                    Map<String, Object> dishMap = new java.util.HashMap<>();
                    try {
                        java.beans.BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(Dish.class);
                        java.beans.PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                        for (java.beans.PropertyDescriptor pd : propertyDescriptors) {
                            if (!"class".equals(pd.getName())) {
                                Object value = pd.getReadMethod().invoke(dish);
                                // 将 BigDecimal 类型的价格转换为 Double 类型，方便前端解析
                                if (pd.getName().equals("price") && value != null) {
                                    dishMap.put(pd.getName(), ((BigDecimal) value).doubleValue());
                                } else {
                                    dishMap.put(pd.getName(), value);
                                }
                            }
                        }
                        // 添加菜品在菜单中的状态（转换为前端期望的格式：1→online，0→offline）
                        String menuStatus = menuDish.getStatus() == 1 ? "online" : "offline";
                        dishMap.put("status", menuStatus);
                        // 保留菜品的全局状态（true=上架，false=下架）
                        dishMap.put("globalStatus", dish.getStatus());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return dishMap;
                })
                .filter(dishMap -> dishMap != null) // 过滤掉可能已删除的菜品
                .collect(java.util.stream.Collectors.toList());

        return ResponseResult.success(dishes);
    }

    /**
     * 更新菜品在菜单中的状态
     */
    @PutMapping("/menu/{menuId}/dishes/{dishId}/status")
    public ResponseResult<?> updateDishStatusInMenu(
            @PathVariable String menuId,
            @PathVariable String dishId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer status = (Integer) request.get("status");

            // 当要上架菜品时，先检查该菜品的全局状态
            if (status == 1) {
                Dish dish = dishService.getById(dishId);
                if (dish == null || !Boolean.TRUE.equals(dish.getStatus())) {
                    return ResponseResult.fail("400", "该菜品未在菜品管理中上架，无法在菜单中上架");
                }
            }

            boolean success = menuService.updateDishStatusInMenu(menuId, dishId, status);
            if (success) {
                return ResponseResult.success("菜品状态更新成功");
            } else {
                return ResponseResult.fail("500", "菜品状态更新失败");
            }
        } catch (Exception e) {
            return ResponseResult.fail("500", "更新失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新菜品在多个菜单中的状态
     */
    @PutMapping("/dishes/{dishId}/status")
    public ResponseResult<?> batchUpdateDishStatusInMenus(
            @PathVariable String dishId,
            @RequestBody Map<String, Object> request) {
        try {
            List<String> menuIds = (List<String>) request.get("menuIds");
            Integer status = (Integer) request.get("status");
            boolean success = menuService.batchUpdateDishStatusInMenus(dishId, menuIds, status);
            if (success) {
                return ResponseResult.success("批量更新成功");
            } else {
                return ResponseResult.fail("500", "批量更新失败");
            }
        } catch (Exception e) {
            return ResponseResult.fail("500", "更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取菜品关联的所有菜单信息
     */
    @GetMapping("/dishes/{dishId}/menus")
    public ResponseResult<?> getMenusByDishId(@PathVariable String dishId) {
        try {
            log.info("获取菜品关联的所有菜单信息 菜品id {}", dishId);
            List<MenuWithDishStatusDTO> menus = menuService.getMenusByDishId(dishId);
            log.info("获取菜品关联的所有菜单信息 {}", menus);
            return ResponseResult.success(menus);
        } catch (Exception e) {
            log.error("获取菜品关联的所有菜单信息失败", e);
            return ResponseResult.fail("500", "查询失败: " + e.getMessage());
        }
    }

    /**
     * 设置菜单自动上下架时间
     */
    @PutMapping("/menu/{menuId}/schedule")
    public ResponseResult<?> setMenuSchedule(@PathVariable String menuId, @RequestBody Map<String, Object> params) {
        try {
            LocalDateTime autoStartTime = LocalDateTime.parse((String) params.get("autoStartTime"));
            LocalDateTime autoEndTime = LocalDateTime.parse((String) params.get("autoEndTime"));
            boolean success = menuService.setMenuSchedule(menuId, autoStartTime, autoEndTime);
            if (success) {
                return ResponseResult.success("自动上下架时间设置成功");
            }
            return ResponseResult.fail("500", "自动上下架时间设置失败");
        } catch (DateTimeParseException e) {
            return ResponseResult.fail("400", "时间格式错误");
        }
    }

    /**
     * 菜单批量操作
     */
    @PostMapping("/menu/batch")
    public ResponseResult<?> batchOperateMenus(@RequestBody Map<String, Object> params) {
        // 将菜单ID转换为String类型
        List<String> menuIds = ((List<?>) params.get("menuIds")).stream()
            .map(Object::toString)
            .collect(java.util.stream.Collectors.toList());
        String action = (String) params.get("action");
        boolean success = menuService.batchOperateMenus(menuIds, action);
        if (success) {
            return ResponseResult.success("批量操作成功");
        }
        return ResponseResult.fail("500", "批量操作失败");
    }

    /**
     * 添加新菜单
     */
    @PostMapping("/merchants/{merchantId}/menu")
    public ResponseResult<?> addMenu(@PathVariable String merchantId, @RequestBody Menu menu) {
        try {
            // 设置商家ID
            menu.setMerchantId(merchantId);

            // 转换前端传来的状态值到后端统一格式
            String frontStatus = menu.getStatus();
            if ("online".equals(frontStatus) || "active".equals(frontStatus)) {
                menu.setStatus("active");
            } else { // draft或offline都转为inactive
                menu.setStatus("inactive");
            }

            boolean success = menuService.save(menu);
            if (success) {
                // 返回前端需要的状态格式
                Menu responseMenu = menuService.getById(menu.getId());
                if (responseMenu != null) {
                    // 将后端的active/inactive转换为前端的online/offline
                    if ("active".equals(responseMenu.getStatus())) {
                        responseMenu.setStatus("online");
                    } else {
                        responseMenu.setStatus("offline");
                    }
                    return ResponseResult.success(responseMenu);
                }
                return ResponseResult.fail("500", "保存菜单失败");
            }
            return ResponseResult.fail("500", "保存菜单失败");
        } catch (DateTimeParseException e) {
            // 处理时间格式解析异常
            return ResponseResult.fail("400", "时间格式错误，请使用正确的日期时间格式");
        } catch (Exception e) {
            return ResponseResult.fail("500", "保存菜单失败: " + e.getMessage());
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping("/merchants/{merchantId}/menu/{menuId}")
    public ResponseResult<?> updateMenu(@PathVariable String merchantId, @PathVariable String menuId, @RequestBody Map<String, Object> requestData) {
        log.info("Received update menu request: {}", requestData);
        try {
            // 验证菜单是否属于该商家
            Menu existingMenu = menuService.getById(menuId);
            if (existingMenu == null || !merchantId.equals(existingMenu.getMerchantId())) {
                return ResponseResult.fail("404", "菜单不存在");
            }

            // 从请求数据中获取菜单基本信息
            Menu menu = new Menu();
            menu.setId(menuId);
            menu.setMerchantId(merchantId);
            menu.setName((String) requestData.get("name"));
            menu.setType((String) requestData.get("category")); // 前端使用category，后端存储为type
            menu.setDescription((String) requestData.get("description")); // 菜单描述

            // 转换前端传来的状态值到后端统一格式
            String frontStatus = (String) requestData.get("status");
            if ("online".equals(frontStatus) || "active".equals(frontStatus)) {
                menu.setStatus("active");
            } else { // draft或offline都转为inactive
                menu.setStatus("inactive");
            }

            // 处理自动时间
            if (requestData.get("autoOnline") != null && !requestData.get("autoOnline").toString().isEmpty()) {
                // 处理时间格式，假设前端传来的是 HH:mm:ss 格式
                String autoOnlineStr = (String) requestData.get("autoOnline");
                // 如果是 HH:mm:ss 格式，需要拼接日期
                if (autoOnlineStr.length() == 8 && autoOnlineStr.contains(":")) {
                    LocalDateTime now = LocalDateTime.now();
                    String[] timeParts = autoOnlineStr.split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    int second = Integer.parseInt(timeParts[2]);
                    menu.setAutoStartTime(now.withHour(hour).withMinute(minute).withSecond(second));
                }
            }

            if (requestData.get("autoOffline") != null && !requestData.get("autoOffline").toString().isEmpty()) {
                String autoOfflineStr = (String) requestData.get("autoOffline");
                if (autoOfflineStr.length() == 8 && autoOfflineStr.contains(":")) {
                    LocalDateTime now = LocalDateTime.now();
                    String[] timeParts = autoOfflineStr.split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    int second = Integer.parseInt(timeParts[2]);
                    menu.setAutoEndTime(now.withHour(hour).withMinute(minute).withSecond(second));
                }
            }

            // 更新菜单基本信息
            boolean success = menuService.updateById(menu);

            // 处理菜品关联
            if (success && requestData.containsKey("dishes")) {
                // 删除旧的菜单菜品关联
                LambdaQueryWrapper<MenuDish> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(MenuDish::getMenuId, menuId);
                menuDishService.remove(deleteWrapper);

                // 添加新的菜单菜品关联
                List<?> dishes = (List<?>) requestData.get("dishes");
                for (int i = 0; i < dishes.size(); i++) {
                    Object dishObj = dishes.get(i);
                    String dishId = null;
                    Integer status = 1; // 默认上架

                    if (dishObj instanceof Map) {
                        // 如果是对象格式，获取id和status
                        Map<String, Object> dishMap = (Map<String, Object>) dishObj;
                        Object idObj = dishMap.get("id");
                        Object statusObj = dishMap.get("status");

                        if (idObj instanceof Number) {
                            dishId = String.valueOf(idObj);
                        } else if (idObj instanceof String) {
                            dishId = (String) idObj;
                        }

                        if (statusObj != null) {
                            if (statusObj instanceof Number) {
                                status = ((Number) statusObj).intValue();
                            } else if (statusObj instanceof String) {
                                try {
                                    status = Integer.parseInt((String) statusObj);
                                } catch (NumberFormatException e) {
                                    // 如果无法解析，保持默认值1
                                    status = 1;
                                }
                            }
                        }
                    } else if (dishObj instanceof Number) {
                        // 兼容旧格式（只传id）
                        dishId = String.valueOf(dishObj);
                    } else if (dishObj instanceof String) {
                        // 兼容旧格式（只传id）
                        dishId = (String) dishObj;
                    }

                    if (dishId != null && !dishId.trim().isEmpty()) {
                        MenuDish menuDish = new MenuDish();
                        menuDish.setMenuId(menuId);
                        menuDish.setDishId(dishId);
                        menuDish.setSort(i); // 设置排序
                        menuDish.setStatus(status); // 设置菜品在菜单中的状态
                        menuDishService.save(menuDish);
                    }
                }
            }

            if (success) {
                // 返回前端需要的状态格式
                Menu responseMenu = menuService.getById(menuId);
                if (responseMenu != null) {
                    // 将后端的active/inactive转换为前端的online/offline
                    if ("active".equals(responseMenu.getStatus())) {
                        responseMenu.setStatus("online");
                    } else {
                        responseMenu.setStatus("offline");
                    }
                    return ResponseResult.success(responseMenu);
                }
                return ResponseResult.fail("500", "更新菜单失败");
            }
            return ResponseResult.fail("500", "更新菜单失败");
        } catch (DateTimeParseException e) {
            return ResponseResult.fail("400", "时间格式错误，请使用正确的日期时间格式");
        } catch (Exception e) {
            return ResponseResult.fail("500", "更新菜单失败: " + e.getMessage());
        }
    }

    /**
     * 解析食材JSON字符串
     * 支持多种JSON格式，返回前端需要的格式
     */
    private Map<String, Object> parseIngredients(String ingredientsJson) {
        Map<String, Object> result = new HashMap<>();
        List<Object> optionalIngredients = new ArrayList<>();
        List<String> requiredIngredients = new ArrayList<>();

        // 如果食材为空或null，返回空数组
        if (ingredientsJson == null || ingredientsJson.trim().isEmpty()) {
            result.put("optionalIngredients", optionalIngredients);
            result.put("requiredIngredients", requiredIngredients);
            return result;
        }

        try {
            // 创建 ObjectMapper 实例，配置为忽略未知属性
            ObjectMapper objectMapper = new ObjectMapper();
            // 尝试解析为Map，使用 Map<String, Object> 类型
            @SuppressWarnings("unchecked")
            Map<String, Object> ingredientsMap = objectMapper.readValue(ingredientsJson, Map.class);

            // 处理必选食材
            Object required = ingredientsMap.get("requiredIngredients");
            if (required == null) {
                required = ingredientsMap.get("required");
            }
            if (required instanceof List) {
                for (Object item : (List<?>) required) {
                    if (item != null) {
                        requiredIngredients.add(item.toString());
                    }
                }
            }

            // 处理可选食材
            Object optional = ingredientsMap.get("optionalIngredients");
            if (optional == null) {
                optional = ingredientsMap.get("optional");
            }
            if (optional instanceof List) {
                for (Object item : (List<?>) optional) {
                    if (item != null) {
                        // 如果是 Map，转换为可选食材对象
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> ingredientMap = (Map<String, Object>) item;
                            Map<String, Object> ingredient = new HashMap<>();
                            ingredient.put("id", ingredientMap.get("id"));
                            ingredient.put("name", ingredientMap.get("name"));
                            ingredient.put("price", ingredientMap.get("price"));
                            ingredient.put("selected", false);
                            optionalIngredients.add(ingredient);
                        } else {
                            optionalIngredients.add(item);
                        }
                    }
                }
            }

        } catch (Exception e) {
            // JSON解析失败，返回空数组
            System.err.println("解析食材JSON失败: " + ingredientsJson + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }

        result.put("optionalIngredients", optionalIngredients);
        result.put("requiredIngredients", requiredIngredients);
        return result;
    }
}
