package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.entity.UserProfile;

import java.util.List;

/**
 * 用户画像服务接口
 */
public interface UserProfileService {

    /**
     * 获取用户画像
     */
    UserProfile getUserProfile(String userId);

    /**
     * 创建用户画像
     */
    boolean createUserProfile(UserProfile profile);

    /**
     * 更新用户画像
     */
    boolean updateUserProfile(UserProfile profile);

    /**
     * 删除用户画像
     */
    boolean deleteUserProfile(String userId);

    /**
     * 根据用户行为实时更新画像（轻量级）
     */
    void updateUserProfileOnBehavior(String userId, String behaviorType, String itemId);

    /**
     * 批量计算用户画像（全量更新）
     */
    UserProfile calculateUserProfile(String userId);

    /**
     * 定时批量更新所有用户画像
     */
    void batchUpdateAllProfiles();

    /**
     * 获取需要更新的用户画像列表
     */
    List<UserProfile> getProfilesNeedUpdate();
}
