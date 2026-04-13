package com.xx.jaseatschoicejava.service;

import java.util.List;
import java.util.Map;

/**
 * 定位服务接口
 */
public interface LocationService {
    /**
     * 获取当前定位
     * @param latitude 纬度
     * @param longitude 经度
     * @return 定位信息
     */
    Map<String, Object> getCurrentLocation(Double latitude, Double longitude);

    /**
     * 地址转坐标
     * @param address 地址
     * @param city 城市（可选）
     * @return 坐标信息
     */
    Map<String, Object> geocode(String address, String city);

    /**
     * 坐标转地址
     * @param lng 经度
     * @param lat 纬度
     * @return 地址信息
     */
    Map<String, Object> reverseGeocode(String lng, String lat);

    /**
     * 获取级联选择器地址数据
     * @return 地址数据列表
     */
    List<Map<String, Object>> getCascaderLocationData();

    /**
     * 地址搜索
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    List<Map<String, Object>> searchAddress(String keyword);
}
