package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Address;
import com.xx.jaseatschoicejava.mapper.AddressMapper;
import com.xx.jaseatschoicejava.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户地址服务实现
 *
 * 缓存策略：
 * - 用户地址列表：缓存1小时
 * - 添加/更新/删除地址：清除缓存
 */
@Slf4j
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    private static final String CACHE_NAME = "address:list";

    @Override
    @Cacheable(value = CACHE_NAME, key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Address> getAddressesByUserId(String userId) {
        log.debug("从数据库查询用户地址列表: userId={}", userId);
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId);
        return list(queryWrapper);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "#address.userId")
    public boolean addAddress(Address address) {
        log.debug("添加地址并清除缓存: userId={}", address.getUserId());
        // 设置创建时间和更新时间
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        // 如果设置为默认地址，先将其他地址设置为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            UpdateWrapper<Address> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", address.getUserId())
                    .set("is_default", 0);
            update(updateWrapper);
        }

        return save(address);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "#address.userId")
    public boolean updateAddress(Address address) {
        log.debug("更新地址并清除缓存: userId={}", address.getUserId());
        // 设置更新时间
        address.setUpdateTime(LocalDateTime.now());

        // 如果设置为默认地址，先将其他地址设置为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            UpdateWrapper<Address> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", address.getUserId())
                    .set("is_default", 0);
            update(updateWrapper);
        }

        return updateById(address);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "#userId")
    public boolean deleteAddress(String addressId, String userId) {
        log.debug("删除地址并清除缓存: userId={}", userId);
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getId, addressId)
                .eq(Address::getUserId, userId);
        return remove(queryWrapper);
    }
}
