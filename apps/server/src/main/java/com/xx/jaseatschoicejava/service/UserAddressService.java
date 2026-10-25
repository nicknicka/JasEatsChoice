package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.UserAddress;

/**
 * 用户地址Service接口
 */
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 获取用户的所有地址
     */
    java.util.List<UserAddress> getUserAddresses(String userId);

    /**
     * 获取用户的默认地址
     */
    UserAddress getDefaultAddress(String userId);

    /**
     * 设置默认地址
     */
    boolean setDefaultAddress(String addressId, String userId);

    /**
     * 添加地址
     */
    UserAddress addAddress(UserAddress address);

    /**
     * 更新地址
     */
    boolean updateAddress(UserAddress address);

    /**
     * 删除地址
     */
    boolean deleteAddress(String addressId, String userId);
}
