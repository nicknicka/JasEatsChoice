package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 天气控制器
 */
@RestController
@RequestMapping("/v1/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    /**
     * 获取天气信息
     */
    @GetMapping
    public ResponseResult<?> getWeatherInfo(@RequestParam String city) {
        // 调用天气服务获取天气信息
        Map<String, Object> weather = weatherService.getWeatherInfo(city);
        return ResponseResult.success(weather);
    }
}
