package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.UserPreference;
import com.xx.jaseatschoicejava.mapper.UserPreferenceMapper;
import com.xx.jaseatschoicejava.service.UserPreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 用户推荐偏好服务实现
 *
 * 缓存策略：
 * - 查询用户偏好：缓存30分钟
 * - 更新用户偏好：刷新缓存
 * - 删除用户偏好：清除缓存
 */
@Slf4j
@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {

    /**
     * 缓存名称
     */
    private static final String CACHE_NAME = "user:preference";

    @Override
    @Cacheable(value = CACHE_NAME, key = "#userId", unless = "#result == null")
    public UserPreference getByUserId(String userId) {
        log.debug("从数据库查询用户偏好: userId={}", userId);
        LambdaQueryWrapper<UserPreference> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPreference::getUserId, userId);
        return getOne(queryWrapper);
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "#preference.userId")
    public boolean updatePreference(UserPreference preference) {
        log.debug("更新用户偏好: userId={}", preference.getUserId());
        // 如果是新增，直接保存
        if (preference.getId() == null) {
            return save(preference);
        }
        // 如果是更新，根据ID更新
        return updateById(preference);
    }

    /**
     * 清除用户偏好缓存
     *
     * @param userId 用户ID
     */
    @CacheEvict(value = CACHE_NAME, key = "#userId")
    public void evictUserPreferenceCache(String userId) {
        log.debug("清除用户偏好缓存: userId={}", userId);
    }
}
