package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.entity.UserCollection;
import com.xx.jaseatschoicejava.service.CollectionService;
import com.xx.jaseatschoicejava.common.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏Controller
 */
@RestController
@RequestMapping("/v1/collections")
public class CollectionController {

    private final CollectionService collectionService;

    @Autowired
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    /**
     * 根据用户ID获取所有收藏
     */
    @GetMapping
    public ResponseResult<?> getCollectionsByUserId(@RequestParam String userId) {
        List<UserCollection> collections = collectionService.getCollectionsByUserId(userId);
        return ResponseResult.success(collections);
    }

    /**
     * 根据用户ID和类型获取收藏
     */
    @GetMapping("/type")
    public ResponseResult<?> getCollectionsByType(@RequestParam String userId, @RequestParam String type) {
        List<UserCollection> collections = collectionService.getCollectionsByUserIdAndType(userId, type);
        return ResponseResult.success(collections);
    }

    /**
     * 收藏
     */
    @PostMapping
    public ResponseResult<?> addCollection(@RequestBody UserCollection collection) {
        Long id = collectionService.addCollection(collection);
        return ResponseResult.success(id);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public ResponseResult<?> removeCollection(
            @RequestParam String userId,
            @RequestParam String type,
            @RequestParam Long id) {
        boolean success = collectionService.removeCollection(userId, type, id);
        return success ? ResponseResult.success() : ResponseResult.fail("500", "取消收藏失败");
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public ResponseResult<?> checkCollection(
            @RequestParam String userId,
            @RequestParam String type,
            @RequestParam Long id) {
        boolean isCollected = collectionService.isCollected(userId, type, id);
        return ResponseResult.success(isCollected);
    }
}
