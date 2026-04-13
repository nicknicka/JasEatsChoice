package com.xx.jaseatschoicejava.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.LocationService;

/**
 * 定位控制器
 */
@RestController
@RequestMapping("/v1/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * 获取当前定位
     * @param latitude 纬度
     * @param longitude 经度
     * @return 定位信息
     */
    @GetMapping
    public ResponseResult<?> getCurrentLocation(@RequestParam(required = false) Double latitude, @RequestParam(required = false) Double longitude) {
        // 调用定位服务获取当前定位
        Map<String, Object> location = locationService.getCurrentLocation(latitude, longitude);
        return ResponseResult.success(location);
    }

    /**
     * 地址转坐标
     */
    @GetMapping("/geocode")
    public ResponseResult<?> geocode(
            @RequestParam String address,
            @RequestParam(required = false) String city
    ) {
        Map<String, Object> location = locationService.geocode(address, city);
        return ResponseResult.success(location);
    }

    /**
     * 坐标转地址
     */
    @GetMapping("/reverse-geocode")
    public ResponseResult<?> reverseGeocode(
            @RequestParam String lng,
            @RequestParam String lat
    ) {
        Map<String, Object> location = locationService.reverseGeocode(lng, lat);
        return ResponseResult.success(location);
    }

    /**
     * 获取级联选择器地址数据
     */
    @GetMapping("/cascader")
    public ResponseResult<?> getCascaderLocationData() {
        // 调用定位服务获取级联选择器地址数据
        List<Map<String, Object>> cascaderData = locationService.getCascaderLocationData();
        return ResponseResult.success(cascaderData);
    }

    /**
     * 地址搜索
     * @param address 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseResult<?> searchAddress(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String keywords
    ) {
        String query = address != null && !address.isEmpty() ? address : keywords;
        List<Map<String, Object>> searchResults = locationService.searchAddress(query);
        return ResponseResult.success(searchResults);
    }
}
