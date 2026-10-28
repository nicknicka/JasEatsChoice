package com.xx.jaseatschoicejava.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.WeatherService;

/**
 * 天气服务实现
 */
@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private static final String WEATHER_KEY_MISSING = "WEATHER_KEY_MISSING";
    private static final String WEATHER_UPSTREAM_ERROR = "WEATHER_UPSTREAM_ERROR";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gaode.map.api.key:}")
    private String gaodeApiKey;

    @Value("${gaode.map.api.weatherUrl:}")
    private String gaodeApiWeatherUrl;

    @Override
    public Map<String, Object> getWeatherInfo(String city) {
        if (!hasText(gaodeApiKey)) {
            logger.error("天气查询失败: gaode.map.api.key 未配置");
            throw new BusinessException(WEATHER_KEY_MISSING, "天气服务未配置 AMAP_API_KEY");
        }

        if (!hasText(city)) {
            throw new BusinessException("400", "城市不能为空");
        }

        try {
            String url = String.format("%s?key=%s&city=%s&extensions=base", gaodeApiWeatherUrl, gaodeApiKey, city);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            String response = responseEntity.getBody();

            if (!hasText(response)) {
                throw new BusinessException(WEATHER_UPSTREAM_ERROR, "天气服务未返回数据");
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});

            if (!"1".equals(String.valueOf(responseMap.get("status")))) {
                logger.warn("高德天气接口返回失败: status={}, info={}, infocode={}",
                        responseMap.get("status"), responseMap.get("info"), responseMap.get("infocode"));
                throw new BusinessException(WEATHER_UPSTREAM_ERROR, "天气服务返回失败: " + responseMap.getOrDefault("info", "未知错误"));
            }

            Object livesValue = responseMap.get("lives");
            if (!(livesValue instanceof List<?> lives) || lives.isEmpty()) {
                throw new BusinessException(WEATHER_UPSTREAM_ERROR, "天气服务未返回实况数据");
            }

            Map<String, Object> firstLife = asMap(lives.get(0));
            if (firstLife == null) {
                throw new BusinessException(WEATHER_UPSTREAM_ERROR, "天气服务未返回实况数据");
            }

            Map<String, Object> weather = new HashMap<>();
            weather.put("city", firstLife.getOrDefault("city", city));
            weather.put("temperature", firstLife.getOrDefault("temperature", ""));
            weather.put("humidity", firstLife.getOrDefault("humidity", ""));
            weather.put("condition", firstLife.getOrDefault("weather", ""));
            weather.put("windSpeed", firstLife.getOrDefault("windpower", ""));
            weather.put("windDirection", firstLife.getOrDefault("winddirection", ""));
            weather.put("reportTime", firstLife.getOrDefault("reporttime", ""));
            return weather;
        } catch (BusinessException e) {
            throw e;
        } catch (RestClientException | java.io.IOException e) {
            logger.error("从高德地图API获取天气信息失败: {}", e.getMessage());
            throw new BusinessException(WEATHER_UPSTREAM_ERROR, "天气服务调用失败");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            return (Map<String, Object>) mapValue;
        }
        return null;
    }
}
