package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.CartItem;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.security.CurrentIdentityResolver;
import com.xx.jaseatschoicejava.service.CartItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器。
 *
 * <p>归属一律取验签身份中的用户ID；路径里的 {@code userId} 必须与之相同，
 * 防止拿别人的 userId 读写他人购物车。
 */
@Slf4j
@Api(tags = "购物车")
@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemService cartItemService;
    private final CurrentIdentityResolver identityResolver;

    /**
     * 加入购物车
     */
    @ApiOperation("加入购物车")
    @PostMapping("/items")
    public ResponseResult<?> addItem(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String userId = identityResolver.requireUserId(request);

        String dishId = str(body.get("dishId"));
        if (dishId == null) {
            return ResponseResult.fail("400", "菜品ID不能为空");
        }

        Integer quantity = null;
        Object rawQuantity = body.get("quantity");
        if (rawQuantity instanceof Number number) {
            quantity = number.intValue();
        } else if (rawQuantity != null) {
            try {
                quantity = Integer.parseInt(String.valueOf(rawQuantity).trim());
            } catch (NumberFormatException e) {
                return ResponseResult.fail("400", "数量格式不正确");
            }
        }

        @SuppressWarnings("unchecked")
        List<String> ingredients = body.get("optionalIngredients") instanceof List<?> list
            ? (List<String>) list
            : null;

        try {
            CartItem item = cartItemService.addItem(userId, dishId, str(body.get("merchantId")),
                quantity, ingredients, str(body.get("spec")), str(body.get("remark")));
            return ResponseResult.success(item);
        } catch (BusinessException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取购物车（按商家分组）
     */
    @ApiOperation("获取购物车")
    @GetMapping("/{userId}")
    public ResponseResult<?> getCart(@PathVariable String userId, HttpServletRequest request) {
        String currentUserId = identityResolver.requireUserId(request);
        if (!currentUserId.equals(userId)) {
            return ResponseResult.fail("403", "无权查看他人的购物车");
        }
        return ResponseResult.success(cartItemService.listGroupedByMerchant(currentUserId));
    }

    /**
     * 修改购物车项
     */
    @ApiOperation("修改购物车项")
    @PutMapping("/items/{itemId}")
    public ResponseResult<?> updateItem(@PathVariable String itemId,
                                        @RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        String userId = identityResolver.requireUserId(request);

        Integer quantity = null;
        Object rawQuantity = body.get("quantity");
        if (rawQuantity instanceof Number number) {
            quantity = number.intValue();
        } else if (rawQuantity != null) {
            try {
                quantity = Integer.parseInt(String.valueOf(rawQuantity).trim());
            } catch (NumberFormatException e) {
                return ResponseResult.fail("400", "数量格式不正确");
            }
        }

        try {
            CartItem item = cartItemService.updateItem(userId, itemId, quantity,
                str(body.get("spec")), str(body.get("remark")));
            if (item == null) {
                return ResponseResult.fail("404", "购物车项不存在");
            }
            return ResponseResult.success(item);
        } catch (BusinessException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 按商家 + 菜品删除：删掉该菜品在此商家下的全部规格。
     *
     * <p>单独给一个端点，而不是让调用方拼一个占位的 itemId。
     */
    @ApiOperation("按菜品移出购物车")
    @DeleteMapping("/items")
    public ResponseResult<?> deleteByDish(@RequestParam String merchantId,
                                          @RequestParam String dishId,
                                          HttpServletRequest request) {
        String userId = identityResolver.requireUserId(request);
        int removed = cartItemService.deleteByDish(userId, merchantId, dishId);
        return removed > 0
            ? ResponseResult.success("已从购物车移除")
            : ResponseResult.fail("404", "购物车中没有该菜品");
    }

    /**
     * 删除购物车项。
     *
     * <p>不带 query 时按 itemId 删除；带 {@code merchantId + dishId} 时删除该菜品在此商家下的全部规格。
     */
    @ApiOperation("删除购物车项")
    @DeleteMapping("/items/{itemId}")
    public ResponseResult<?> deleteItem(@PathVariable String itemId,
                                        @RequestParam(required = false) String merchantId,
                                        @RequestParam(required = false) String dishId,
                                        HttpServletRequest request) {
        String userId = identityResolver.requireUserId(request);

        if (merchantId != null && dishId != null) {
            int removed = cartItemService.deleteByDish(userId, merchantId, dishId);
            return removed > 0
                ? ResponseResult.success("已从购物车移除")
                : ResponseResult.fail("404", "购物车中没有该菜品");
        }

        return cartItemService.deleteItem(userId, itemId)
            ? ResponseResult.success("已删除")
            : ResponseResult.fail("404", "购物车项不存在");
    }

    /**
     * 批量删除
     */
    @ApiOperation("批量删除购物车项")
    @PostMapping("/batch-delete")
    public ResponseResult<?> batchDelete(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String userId = identityResolver.requireUserId(request);
        if (!(body.get("cartIds") instanceof List<?> raw) || raw.isEmpty()) {
            return ResponseResult.fail("400", "购物车项ID不能为空");
        }
        List<String> ids = raw.stream().map(String::valueOf).toList();
        int removed = cartItemService.batchDelete(userId, ids);
        return removed > 0
            ? ResponseResult.success("已删除 " + removed + " 项")
            : ResponseResult.fail("404", "没有可删除的购物车项");
    }

    /**
     * 清空购物车
     */
    @ApiOperation("清空购物车")
    @DeleteMapping("/{userId}/clear")
    public ResponseResult<?> clear(@PathVariable String userId,
                                   @RequestParam(required = false) String merchantId,
                                   HttpServletRequest request) {
        String currentUserId = identityResolver.requireUserId(request);
        if (!currentUserId.equals(userId)) {
            return ResponseResult.fail("403", "无权操作他人的购物车");
        }
        cartItemService.clear(currentUserId, merchantId);
        return ResponseResult.success("购物车已清空");
    }

    /**
     * 选中 / 取消选中单个购物车项
     */
    @ApiOperation("选中购物车项")
    @PutMapping("/{itemId}/select")
    public ResponseResult<?> selectItem(@PathVariable String itemId,
                                        @RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        String userId = identityResolver.requireUserId(request);
        boolean selected = Boolean.TRUE.equals(body.get("selected")) || "true".equals(String.valueOf(body.get("selected")));
        return cartItemService.selectItem(userId, itemId, selected)
            ? ResponseResult.success("已更新选中状态")
            : ResponseResult.fail("404", "购物车项不存在");
    }

    /**
     * 全选 / 取消全选
     */
    @ApiOperation("全选购物车项")
    @PutMapping("/select-all")
    public ResponseResult<?> selectAll(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String userId = identityResolver.requireUserId(request);
        boolean selected = Boolean.TRUE.equals(body.get("selected")) || "true".equals(String.valueOf(body.get("selected")));
        int updated = cartItemService.selectAll(userId, str(body.get("merchantId")), selected);
        return ResponseResult.success("已更新 " + updated + " 项");
    }

    /** 取非空白字符串，空白视为 null。 */
    private String str(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
