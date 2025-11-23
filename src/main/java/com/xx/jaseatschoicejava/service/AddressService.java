package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Address;

import java.util.List;

/**
 * 用户地址服务
 */
public interface AddressService extends IService<Address> {

    /**
     * 根据用户ID获取地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> getAddressesByUserId(Long userId);

    /**
     * 新增地址
     * @param address 地址信息
     * @return 新增是否成功
     */
    boolean addAddress(Address address);

    /**
     * 更新地址
     * @param address 地址信息
     * @return 更新是否成功
     */
    boolean updateAddress(Address address);

    /**
     * 删除地址
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 删除是否成功
     */
    boolean deleteAddress(Long addressId, Long userId);
}
