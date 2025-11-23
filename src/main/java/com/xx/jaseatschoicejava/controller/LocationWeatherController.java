package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 定位与天气控制器
 */
@RestController
@RequestMapping("/api/v1")
public class LocationWeatherController {

    /**
     * 获取当前定位
     */
    @GetMapping("/location")
    public ResponseResult<?> getCurrentLocation() {
        // TODO: Integrate with third-party location API
        // For now, return mock location data

        Map<String, Object> location = new HashMap<>();
        location.put("city", "上海");
        location.put("district", "浦东新区");
        location.put("longitude", 121.4737);
        location.put("latitude", 31.2304);
        location.put("address", "上海市浦东新区陆家嘴");

        return ResponseResult.success(location);
    }

    /**
     * 获取天气信息
     */
    @GetMapping("/weather")
    public ResponseResult<?> getWeatherInfo(@RequestParam String city) {
        // TODO: Integrate with third-party weather API
        // For now, return mock weather data

        Map<String, Object> weather = new HashMap<>();
        weather.put("city", city);
        weather.put("temperature", 28);
        weather.put("humidity", 75);
        weather.put("condition", "晴天");
        weather.put("windSpeed", "微风");

        return ResponseResult.success(weather);
    }
}
