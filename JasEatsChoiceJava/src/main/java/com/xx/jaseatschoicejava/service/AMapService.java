package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.config.AMapConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 高德地图服务类
 */
@Service
public class AMapService {

    private final AMapConfig aMapConfig;
    private final RestTemplate restTemplate;

    // 日志
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AMapService.class);

    public AMapService(AMapConfig aMapConfig) {
        this.aMapConfig = aMapConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 地址关键词搜索（使用后端代理）
     * 文档：https://lbs.amap.com/api/webservice/guide/api/search
     * 使用 v3 API（兼容性更好）
     */
    public Map<String, Object> searchAddress(String keywords, String city) {
        try {
            String url = "https://restapi.amap.com/v3/place/text";

            // 构建请求参数
            String apiKey = aMapConfig.getApiKey();
            String cityParam = city != null ? city : "全国";

            // 发送请求 - 直接传参数值，不使用 MultiValueMap
            String result = restTemplate.getForObject(
                url + "?key={key}&keywords={keywords}&city={city}&output=json&offset=0&page=1&limit=10",
                String.class,
                apiKey,
                keywords,
                cityParam
            );

            // 解析 JSON 结果
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(result);

            if ("1".equals(jsonNode.path("status").asText())) {
                // 成功
                JsonNode pois = jsonNode.path("pois");
                List<Map<String, Object>> searchResults = new ArrayList<>();

                for (JsonNode poi : pois) {
                    Map<String, Object> poiData = new HashMap<>();
                    poiData.put("name", poi.path("name").asText());
                    poiData.put("address", poi.path("address").asText(""));
                    poiData.put("pname", poi.path("pname").asText(""));

                    // 提取位置信息 (v3 API 返回格式为 "lng,lat" 字符串)
                    String locationStr = poi.path("location").asText();
                    if (locationStr != null && !locationStr.isEmpty()) {
                        String[] coords = locationStr.split(",");
                        if (coords.length == 2) {
                            try {
                                Map<String, Double> loc = new HashMap<>();
                                loc.put("lng", Double.parseDouble(coords[0]));
                                loc.put("lat", Double.parseDouble(coords[1]));
                                poiData.put("location", loc);
                            } catch (NumberFormatException e) {
                                log.warn("解析坐标失败: {}", locationStr);
                            }
                        }
                    }

                    searchResults.add(poiData);
                }

                return Map.of(
                    "code", "200",
                    "message", "搜索成功",
                    "data", searchResults
                );
            } else {
                // 失败
                String info = jsonNode.path("info").asText();
                String infocode = jsonNode.path("infocode").asText();
                log.error("高德地图搜索失败 - info: {}, infocode: {}", info, infocode);

                return Map.of(
                    "code", "500",
                    "message", "搜索失败: " + info,
                    "data", Collections.emptyList()
                );
            }
        } catch (Exception e) {
            log.error("高德地图搜索异常", e);
            return Map.of(
                "code", "500",
                "message", "搜索异常: " + e.getMessage(),
                "data", Collections.emptyList()
            );
        }
    }

    /**
     * 地理编码（地址 → 坐标）
     */
    public Map<String, Object> geocode(String address, String city) {
        try {
            String url = "https://restapi.amap.com/v3/geocode/geo";
            String apiKey = aMapConfig.getApiKey();

            if (city != null && !city.isEmpty()) {
                ResponseEntity<String> response = restTemplate.getForEntity(
                    url + "?key={key}&address={address}&city={city}",
                    String.class,
                    apiKey,
                    address,
                    city
                );
                return parseGeocodeResponse(response.getBody());
            } else {
                ResponseEntity<String> response = restTemplate.getForEntity(
                    url + "?key={key}&address={address}",
                    String.class,
                    apiKey,
                    address
                );
                return parseGeocodeResponse(response.getBody());
            }
        } catch (Exception e) {
            log.error("高德地图地理编码异常", e);
            return Map.of(
                "code", "500",
                "message", "地理编码异常: " + e.getMessage(),
                "data", null
            );
        }
    }

    /**
     * 解析地理编码响应
     */
    private Map<String, Object> parseGeocodeResponse(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);

            if ("1".equals(jsonNode.path("status").asText())) {
                JsonNode geocodes = jsonNode.path("geocodes");
                if (geocodes != null && geocodes.isArray() && geocodes.size() > 0) {
                    JsonNode geocode = geocodes.get(0);
                    JsonNode location = geocode.path("location");

                    if (location != null) {
                        return Map.of(
                            "code", "200",
                            "message", "地理编码成功",
                            "data", Map.of(
                                "lng", location.path("lng").asText(),
                                "lat", location.path("lat").asText(),
                                "formattedAddress", geocode.path("formattedAddress").asText()
                            )
                        );
                    }
                }
            }

            return Map.of(
                "code", "404",
                "message", "未找到该地址",
                "data", null
            );
        } catch (Exception e) {
            log.error("解析地理编码响应异常", e);
            return Map.of(
                "code", "500",
                "message", "解析响应失败",
                "data", null
            );
        }
    }

    /**
     * 逆地理编码（坐标 → 地址）
     */
    public Map<String, Object> regeocode(String lng, String lat) {
        try {
            String url = "https://restapi.amap.com/v3/geocode/regeo";

            ResponseEntity<String> response = restTemplate.getForEntity(
                url + "?key={key}&location={lng},{lat}&extensions=base",
                String.class,
                aMapConfig.getApiKey(),
                lng,
                lat
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());

            if ("1".equals(jsonNode.path("status").asText())) {
                JsonNode regeocode = jsonNode.path("regeocode");
                if (regeocode != null) {
                    return Map.of(
                        "code", "200",
                        "message", "逆地理编码成功",
                        "data", Map.of(
                            "formattedAddress", regeocode.path("formatted_address").asText()
                        )
                    );
                }
            }

            return Map.of(
                "code", "404",
                "message", "未找到该位置",
                "data", null
            );
        } catch (Exception e) {
            log.error("高德地图逆地理编码异常", e);
            return Map.of(
                "code", "500",
                "message", "逆地理编码异常: " + e.getMessage(),
                "data", null
            );
        }
    }
}
