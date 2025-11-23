package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.UserPreference;

/**
 * 用户推荐偏好服务
 */
public interface UserPreferenceService extends IService<UserPreference> {

    /**
     * 根据用户ID获取推荐偏好
     * @param userId 用户ID
     * @return 用户推荐偏好
     */
    UserPreference getByUserId(Long userId);

    /**
     * 更新用户推荐偏好
     * @param preference 用户推荐偏好
     * @return 是否更新成功
     */
    boolean updatePreference(UserPreference preference);
}
