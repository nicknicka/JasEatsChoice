package com.xx.jaseatschoicejava.service;

import java.util.Map;

/**
 * 天气服务接口
 */
public interface WeatherService {
    /**
     * 获取天气信息
     * @param city 城市名称
     * @return 天气信息
     */
    Map<String, Object> getWeatherInfo(String city);
}
