package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Address;
import com.xx.jaseatschoicejava.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址控制器
 */
@RestController
@RequestMapping("/api/v1/users")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 获取地址列表
     */
    @GetMapping("/{userId}/addresses")
    public ResponseResult<?> getAddresses(@PathVariable Long userId) {
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseResult.success(addresses);
    }

    /**
     * 新增地址
     */
    @PostMapping("/{userId}/addresses")
    public ResponseResult<?> addAddress(@PathVariable Long userId, @RequestBody Address address) {
        // 确保地址属于当前用户
        address.setUserId(userId);
        boolean success = addressService.addAddress(address);
        if (success) {
            return ResponseResult.success("新增地址成功");
        }
        return ResponseResult.fail("500", "新增地址失败");
    }

    /**
     * 更新地址
     */
    @PutMapping("/{userId}/addresses/{addressId}")
    public ResponseResult<?> updateAddress(@PathVariable Long userId, @PathVariable Long addressId, @RequestBody Address address) {
        // 确保地址属于当前用户
        address.setUserId(userId);
        address.setId(addressId);
        boolean success = addressService.updateAddress(address);
        if (success) {
            return ResponseResult.success("更新地址成功");
        }
        return ResponseResult.fail("500", "更新地址失败");
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{userId}/addresses/{addressId}")
    public ResponseResult<?> deleteAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        boolean success = addressService.deleteAddress(addressId, userId);
        if (success) {
            return ResponseResult.success("删除地址成功");
        }
        return ResponseResult.fail("500", "删除地址失败");
    }
}
