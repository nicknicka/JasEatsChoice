package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.UserAddress;
import com.xx.jaseatschoicejava.mapper.UserAddressMapper;
import com.xx.jaseatschoicejava.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户地址Service实现
 */
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public List<UserAddress> getUserAddresses(String userId) {
        return baseMapper.findByUserId(userId);
    }

    @Override
    public UserAddress getDefaultAddress(String userId) {
        return baseMapper.findDefaultAddress(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(String addressId, String userId) {
        // 先取消该用户的所有默认地址
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);
        List<UserAddress> addresses = baseMapper.selectList(queryWrapper);

        for (UserAddress addr : addresses) {
            if (addr.getIsDefault()) {
                addr.setIsDefault(false);
                baseMapper.updateById(addr);
            }
        }

        // 设置新的默认地址
        UserAddress defaultAddr = baseMapper.selectById(addressId);
        if (defaultAddr == null || !defaultAddr.getUserId().equals(userId)) {
            return false;
        }

        defaultAddr.setIsDefault(true);
        return baseMapper.updateById(defaultAddr) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddress addAddress(UserAddress address) {
        // 如果是第一个地址或者设置为默认地址，则设为默认
        if (address.getIsDefault() == null) {
            address.setIsDefault(false);
        }

        if (address.getIsDefault()) {
            // 取消其他默认地址
            setDefaultAddress(null, address.getUserId());
        } else {
            // 检查是否是第一个地址
            LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddress::getUserId, address.getUserId());
            Long count = baseMapper.selectCount(queryWrapper);
            if (count == 0) {
                address.setIsDefault(true);
            }
        }

        baseMapper.insert(address);
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(UserAddress address) {
        UserAddress existing = baseMapper.selectById(address.getId());
        if (existing == null || !existing.getUserId().equals(address.getUserId())) {
            return false;
        }

        if (address.getIsDefault() != null && address.getIsDefault()) {
            setDefaultAddress(address.getId(), address.getUserId());
        }

        return baseMapper.updateById(address) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(String addressId, String userId) {
        UserAddress address = baseMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            return false;
        }

        return baseMapper.deleteById(addressId) > 0;
    }
}
