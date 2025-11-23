package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.UserPreference;
import com.xx.jaseatschoicejava.mapper.UserPreferenceMapper;
import com.xx.jaseatschoicejava.service.UserPreferenceService;
import org.springframework.stereotype.Service;

/**
 * 用户推荐偏好服务实现
 */
@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {

    @Override
    public UserPreference getByUserId(Long userId) {
        LambdaQueryWrapper<UserPreference> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPreference::getUserId, userId);
        return getOne(queryWrapper);
    }

    @Override
    public boolean updatePreference(UserPreference preference) {
        // 如果是新增，直接保存
        if (preference.getId() == null) {
            return save(preference);
        }
        // 如果是更新，根据ID更新
        return updateById(preference);
    }
}
