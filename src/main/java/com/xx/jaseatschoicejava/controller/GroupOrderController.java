package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 群订单控制器
 */
@RestController
@RequestMapping("/api/v1")
public class GroupOrderController {

    @Autowired
    private GroupOrderService groupOrderService;

    /**
     * 创建群订单
     */
    @PostMapping("/group-orders")
    public ResponseResult<?> createGroupOrder(@RequestBody Map<String, Object> request) {
        try {
            // 解析群订单信息
            GroupOrder groupOrder = new GroupOrder();
            groupOrder.setInitiatorId(Long.valueOf(request.get("initiatorId").toString()));
            groupOrder.setMerchantId(Long.valueOf(request.get("merchantId").toString()));
            groupOrder.setGroupId(Long.valueOf(request.get("groupId").toString()));
            groupOrder.setAddressId(Long.valueOf(request.get("addressId").toString()));
            groupOrder.setRemark((String) request.get("remark"));

            // 解析菜品列表
            List<Map<String, Object>> dishItemsMap = (List<Map<String, Object>>) request.get("dishItems");
            List<GroupOrderDish> dishItems = dishItemsMap.stream()
                    .map(item -> {
                        GroupOrderDish dish = new GroupOrderDish();
                        dish.setDishId(Long.valueOf(item.get("dishId").toString()));
                        dish.setQuantity(Integer.valueOf(item.get("quantity").toString()));
                        dish.setCustomization((String) item.get("customization"));
                        dish.setUserId(Long.valueOf(item.get("userId").toString()));
                        return dish;
                    }).toList();

            // 创建群订单
            boolean success = groupOrderService.createGroupOrder(groupOrder, dishItems);
            if (success) {
                return ResponseResult.success(groupOrder.getId()); // 返回群订单ID
            }
            return ResponseResult.fail("500", "创建群订单失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "创建群订单失败：" + e.getMessage());
        }
    }

    /**
     * 获取群订单列表
     */
    @GetMapping("/groups/{groupId}/orders")
    public ResponseResult<?> getGroupOrders(@PathVariable Long groupId,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        List<GroupOrder> groupOrders = groupOrderService.getGroupOrdersByGroupId(groupId);
        // TODO: 实现分页和状态筛选
        return ResponseResult.success(groupOrders);
    }

    /**
     * 获取群订单详情
     */
    @GetMapping("/group-orders/{groupOrderId}")
    public ResponseResult<?> getGroupOrderDetail(@PathVariable Long groupOrderId) {
        GroupOrder groupOrder = groupOrderService.getGroupOrderDetail(groupOrderId);
        if (groupOrder != null) {
            // 获取菜品列表
            List<GroupOrderDish> dishItems = groupOrderService.getGroupOrderDishes(groupOrderId);
            // 构建返回结果
            return ResponseResult.success(Map.of(
                    "groupOrder", groupOrder,
                    "dishItems", dishItems
            ));
        }
        return ResponseResult.fail("404", "群订单不存在");
    }

    /**
     * 同步群订单消息
     */
    @PostMapping("/group-orders/{groupOrderId}/sync-message")
    public ResponseResult<?> syncGroupOrderMessage(@PathVariable Long groupOrderId, @RequestBody Map<String, Object> params) {
        // TODO: Implement message sync logic
        // According to PRD: 商家视角与客户的单聊界面，关联订单信息；商家发送消息后可选择同步至群聊，同步内容以「【订单同步】」开头
        return ResponseResult.success("同步群订单消息功能待实现");
    }
}
