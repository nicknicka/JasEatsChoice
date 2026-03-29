package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.UserCollection;
import com.xx.jaseatschoicejava.service.CollectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏Controller（前端API）
 * 对接前端 /v1/favorites 路径
 * 内部调用 CollectionService
 */
@Api(tags = "收藏管理（前端）")
@RestController
@RequestMapping("/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final CollectionService collectionService;

    /**
     * 获取收藏列表
     */
    @ApiOperation("获取收藏列表")
    @GetMapping
    public ResponseResult<?> getFavorites(@RequestParam String userId, @RequestParam(required = false) String type) {
        List<UserCollection> collections = collectionService.getCollectionsByUserIdAndType(userId, type != null ? type : "dish");
        return ResponseResult.success(collections);
    }

    /**
     * 获取收藏菜品列表
     */
    @ApiOperation("获取收藏菜品列表")
    @GetMapping("/dishes")
    public ResponseResult<?> getDishFavorites(@RequestParam String userId) {
        List<UserCollection> collections = collectionService.getCollectionsByUserIdAndType(userId, "dish");
        return ResponseResult.success(collections);
    }

    /**
     * 获取收藏食谱列表
     */
    @ApiOperation("获取收藏食谱列表")
    @GetMapping("/recipes")
    public ResponseResult<?> getRecipeFavorites(@RequestParam String userId) {
        List<UserCollection> collections = collectionService.getCollectionsByUserIdAndType(userId, "recipe");
        return ResponseResult.success(collections);
    }

    /**
     * 检查菜品是否已收藏
     */
    @ApiOperation("检查菜品是否已收藏")
    @GetMapping("/dishes/{dishId}/check")
    public ResponseResult<?> checkDishFavorite(
            @PathVariable String dishId,
            @RequestParam String userId
    ) {
        try {
            System.out.println("检查菜品收藏状态: userId=" + userId + ", dishId=" + dishId);
            boolean isCollected = collectionService.isCollected(userId, "dish", dishId);
            System.out.println("收藏状态: " + isCollected);
            return ResponseResult.success(isCollected);
        } catch (Exception e) {
            System.err.println("检查收藏状态失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseResult.success(false);
        }
    }

    /**
     * 收藏菜品
     */
    @ApiOperation("收藏菜品")
    @PostMapping("/dishes")
    public ResponseResult<?> addDishFavorite(@RequestBody UserCollection collection) {
        collection.setCollectableType("dish");
        if (collection.getId() == null || collection.getId().isEmpty()) {
            collection.setId("F" + System.currentTimeMillis());
        }
        UserCollection savedCollection = collectionService.addCollection(collection);
        return ResponseResult.success(savedCollection);
    }

    /**
     * 取消收藏菜品
     */
    @ApiOperation("取消收藏菜品")
    @DeleteMapping("/dishes/{dishId}")
    public ResponseResult<?> removeDishFavorite(
            @RequestParam String userId,
            @PathVariable String dishId
    ) {
        boolean success = collectionService.removeCollection(userId, "dish", dishId);
        return success ? ResponseResult.success() : ResponseResult.fail("500", "取消收藏失败");
    }

    /**
     * 收藏食谱
     */
    @ApiOperation("收藏食谱")
    @PostMapping("/recipes")
    public ResponseResult<?> addRecipeFavorite(@RequestBody UserCollection collection) {
        collection.setCollectableType("recipe");
        if (collection.getId() == null || collection.getId().isEmpty()) {
            collection.setId("F" + System.currentTimeMillis());
        }
        UserCollection savedCollection = collectionService.addCollection(collection);
        return ResponseResult.success(savedCollection);
    }

    /**
     * 取消收藏食谱
     */
    @ApiOperation("取消收藏食谱")
    @DeleteMapping("/recipes/{recipeId}")
    public ResponseResult<?> removeRecipeFavorite(
            @RequestParam String userId,
            @PathVariable String recipeId
    ) {
        boolean success = collectionService.removeCollection(userId, "recipe", recipeId);
        return success ? ResponseResult.success() : ResponseResult.fail("500", "取消收藏失败");
    }

    /**
     * 收藏商家
     */
    @ApiOperation("收藏商家")
    @PostMapping("/merchants")
    public ResponseResult<?> addMerchantFavorite(@RequestBody UserCollection collection) {
        collection.setCollectableType("merchant");
        if (collection.getId() == null || collection.getId().isEmpty()) {
            collection.setId("F" + System.currentTimeMillis());
        }
        UserCollection savedCollection = collectionService.addCollection(collection);
        return ResponseResult.success(savedCollection);
    }

    /**
     * 取消收藏商家
     */
    @ApiOperation("取消收藏商家")
    @DeleteMapping("/merchants/{merchantId}")
    public ResponseResult<?> removeMerchantFavorite(
            @RequestParam String userId,
            @PathVariable String merchantId
    ) {
        boolean success = collectionService.removeCollection(userId, "merchant", merchantId);
        return success ? ResponseResult.success() : ResponseResult.fail("500", "取消收藏失败");
    }
}
