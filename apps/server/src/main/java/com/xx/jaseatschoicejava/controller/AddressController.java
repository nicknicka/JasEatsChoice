package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Address;
import com.xx.jaseatschoicejava.mapper.AddressMapper;
import com.xx.jaseatschoicejava.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址控制器
 */
@Api(tags = "用户地址管理")
@RestController
@RequestMapping("/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final AddressMapper addressMapper;

    /**
     * 获取用户地址列表（兼容前端API）
     */
    @ApiOperation("获取用户地址列表")
    @GetMapping("/user")
    public ResponseResult<List<Address>> getUserAddresses(
            @ApiParam("用户ID") @RequestParam String userId) {
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseResult.success(addresses);
    }

    /**
     * 获取默认地址（兼容前端API）
     */
    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public ResponseResult<Address> getDefaultAddress(
            @ApiParam("用户ID") @RequestParam String userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .last("LIMIT 1");
        Address address = addressMapper.selectOne(queryWrapper);
        return ResponseResult.success(address);
    }

    /**
     * 添加地址（兼容前端API）
     */
    @ApiOperation("添加地址")
    @PostMapping
    public ResponseResult<Void> addAddress(@RequestBody Address address) {
        // 如果是第一个地址，自动设为默认
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }

        if (address.getIsDefault() == 1) {
            // 取消其他默认地址
            LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Address::getUserId, address.getUserId())
                    .eq(Address::getIsDefault, 1);
            List<Address> existingDefaults = addressMapper.selectList(queryWrapper);
            for (Address addr : existingDefaults) {
                addr.setIsDefault(0);
                addressMapper.updateById(addr);
            }
        }

        boolean success = addressService.addAddress(address);
        if (success) {
            return ResponseResult.success(null, "添加成功");
        }
        return ResponseResult.fail("400", "添加失败");
    }

    /**
     * 更新地址（兼容前端API）
     */
    @ApiOperation("更新地址")
    @PutMapping("/{id}")
    public ResponseResult<Void> updateAddress(
            @ApiParam("地址ID") @PathVariable String id,
            @RequestBody Address address) {
        address.setId(id);

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            // 取消其他默认地址
            LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Address::getUserId, address.getUserId())
                    .eq(Address::getIsDefault, 1)
                    .ne(Address::getId, id);
            List<Address> existingDefaults = addressMapper.selectList(queryWrapper);
            for (Address addr : existingDefaults) {
                addr.setIsDefault(0);
                addressMapper.updateById(addr);
            }
        }

        boolean success = addressService.updateAddress(address);
        if (success) {
            return ResponseResult.success(null, "更新成功");
        }
        return ResponseResult.fail("400", "更新失败");
    }

    /**
     * 删除地址（兼容前端API）
     */
    @ApiOperation("删除地址")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteAddress(
            @ApiParam("地址ID") @PathVariable String id,
            @ApiParam("用户ID") @RequestParam String userId) {
        boolean success = addressService.deleteAddress(id, userId);
        if (success) {
            return ResponseResult.success(null, "删除成功");
        }
        return ResponseResult.fail("400", "删除失败");
    }

    /**
     * 设置默认地址（兼容前端API）
     */
    @ApiOperation("设置默认地址")
    @PutMapping("/{id}/default")
    public ResponseResult<Void> setDefaultAddress(
            @ApiParam("地址ID") @PathVariable String id,
            @ApiParam("用户ID") @RequestParam String userId) {
        // 取消所有默认地址
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1);
        List<Address> existingDefaults = addressMapper.selectList(queryWrapper);
        for (Address addr : existingDefaults) {
            addr.setIsDefault(0);
            addressMapper.updateById(addr);
        }

        // 设置新的默认地址
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return ResponseResult.fail("400", "地址不存在");
        }
        address.setIsDefault(1);
        addressMapper.updateById(address);

        return ResponseResult.success(null, "设置成功");
    }
}

/**
 * 旧的地址控制器（保持兼容性）
 */
@RestController
@RequestMapping("/v1/users")
class LegacyAddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 获取地址列表
     */
    @GetMapping("/{userId}/addresses")
    public ResponseResult<?> getAddresses(@PathVariable String userId) {
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseResult.success(addresses);
    }

    /**
     * 新增地址
     */
    @PostMapping("/{userId}/addresses")
    public ResponseResult<?> addAddress(@PathVariable String userId, @RequestBody Address address) {
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
    public ResponseResult<?> updateAddress(@PathVariable String userId, @PathVariable String addressId, @RequestBody Address address) {
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
    public ResponseResult<?> deleteAddress(@PathVariable String userId, @PathVariable String addressId) {
        boolean success = addressService.deleteAddress(addressId, userId);
        if (success) {
            return ResponseResult.success("删除地址成功");
        }
        return ResponseResult.fail("500", "删除地址失败");
    }
}
