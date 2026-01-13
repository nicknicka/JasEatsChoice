package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.UserCollection;
import java.util.List;

/**
 * 收藏Service接口
 */
public interface CollectionService {

    /**
     * 根据用户ID获取所有收藏
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<UserCollection> getCollectionsByUserId(String userId);

    /**
     * 根据用户ID和类型获取收藏
     * @param userId 用户ID
     * @param type 收藏类型
     * @return 收藏列表
     */
    List<UserCollection> getCollectionsByUserIdAndType(String userId, String type);

    /**
     * 收藏
     * @param collection 收藏对象
     * @return 收藏ID
     */
    UserCollection addCollection(UserCollection collection);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param collectableType 收藏类型
     * @param collectableId 收藏对象ID
     * @return 是否成功
     */
    boolean removeCollection(String userId, String collectableType, String collectableId);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param collectableType 收藏类型
     * @param collectableId 收藏对象ID
     * @return 是否已收藏
     */
    boolean isCollected(String userId, String collectableType, String collectableId);
}
