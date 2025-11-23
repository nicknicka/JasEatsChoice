package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Address;
import com.xx.jaseatschoicejava.mapper.AddressMapper;
import com.xx.jaseatschoicejava.service.AddressService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户地址服务实现
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId);
        return list(queryWrapper);
    }

    @Override
    public boolean addAddress(Address address) {
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
    public boolean updateAddress(Address address) {
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
    public boolean deleteAddress(Long addressId, Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getId, addressId)
                .eq(Address::getUserId, userId);
        return remove(queryWrapper);
    }
}
