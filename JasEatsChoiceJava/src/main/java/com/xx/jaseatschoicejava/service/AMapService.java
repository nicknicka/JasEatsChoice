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
            return buildResult("500", "地理编码异常: " + e.getMessage(), null);
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
                    String locationStr = geocode.path("location").asText("");

                    if (!locationStr.isEmpty()) {
                        String[] coords = locationStr.split(",");
                        if (coords.length == 2) {
                            String lng = coords[0].trim();
                            String lat = coords[1].trim();

                            return Map.of(
                                "code", "200",
                                "message", "地理编码成功",
                                "data", Map.of(
                                    "lng", lng,
                                    "lat", lat,
                                    "formattedAddress", geocode.path("formatted_address").asText("")
                                )
                            );
                        }
                    }

                    // location 字段解析失败，返回明确错误信息，便于排查第三方接口格式变化
                    log.warn("地理编码返回location字段格式异常: {}", locationStr);
                    return buildResult("500", "地理编码返回坐标格式异常", null);
                }
            }

            return buildResult("404", "未找到该地址", null);
        } catch (Exception e) {
            log.error("解析地理编码响应异常", e);
            return buildResult("500", "解析响应失败", null);
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

            return buildResult("404", "未找到该位置", null);
        } catch (Exception e) {
            log.error("高德地图逆地理编码异常", e);
            return buildResult("500", "逆地理编码异常: " + e.getMessage(), null);
        }
    }

    /**
     * 构建响应结果（支持 null 值，避免 Map.of() 的 NPE 问题）
     */
    private Map<String, Object> buildResult(String code, String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    /**
     * IP 定位（通过 IP 地址获取大概位置）
     * 文档：https://lbs.amap.com/api/webservice/guide/api/ipconfig
     * 精度：城市级别（比 GPS 精确度低，但不需要用户授权）
     */
    public Map<String, Object> ipLocation() {
        try {
            String url = "https://restapi.amap.com/v3/ip";
            String apiKey = aMapConfig.getApiKey();

            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("高德IP定位失败: amap.api-key 未配置");
                return buildResult("500", "IP定位失败: amap.api-key 未配置", null);
            }

            ResponseEntity<String> response = restTemplate.getForEntity(
                url + "?key={key}",
                String.class,
                apiKey
            );

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                log.error("高德IP定位失败: 响应体为空");
                return buildResult("500", "IP定位失败: 高德接口响应为空", null);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);

            log.debug("高德IP定位原始响应: {}", responseBody);

            if ("1".equals(jsonNode.path("status").asText())) {
                String province = jsonNode.path("province").asText("");
                String city = jsonNode.path("city").asText("");

                // 高德 IP 定位 API 直接返回 location 字段（经纬度）
                JsonNode locationNode = jsonNode.path("location");
                if (locationNode != null && !locationNode.isMissingNode() && !locationNode.asText().isEmpty()) {
                    String locationStr = locationNode.asText();
                    String[] coords = locationStr.split(",");
                    if (coords.length == 2) {
                        try {
                            double lng = Double.parseDouble(coords[0]);
                            double lat = Double.parseDouble(coords[1]);

                            Map<String, Object> locationData = new HashMap<>();
                            locationData.put("lng", lng);
                            locationData.put("lat", lat);
                            locationData.put("province", province);
                            locationData.put("city", city);
                            locationData.put("accuracy", "city");

                            log.info("IP定位成功: {} {}, lng={}, lat={}", province, city, lng, lat);

                            return Map.of(
                                "code", "200",
                                "message", "IP定位成功",
                                "data", locationData
                            );
                        } catch (NumberFormatException e) {
                            log.warn("解析坐标失败: {}", locationStr);
                        }
                    }
                }

                // 如果没有坐标，尝试通过城市名做地理编码获取坐标
                if (!province.isEmpty() || !city.isEmpty()) {
                    String address = city.isEmpty() ? province : city;
                    log.info("IP定位无坐标，尝试地理编码: {}", address);

                    try {
                        Map<String, Object> geocodeResult = geocode(address, city);
                        if ("200".equals(geocodeResult.get("code")) && geocodeResult.get("data") != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> geoData = (Map<String, Object>) geocodeResult.get("data");
                            if (geoData.get("lng") != null && geoData.get("lat") != null) {
                                Map<String, Object> locationData = new HashMap<>();
                                locationData.put("lng", geoData.get("lng"));
                                locationData.put("lat", geoData.get("lat"));
                                locationData.put("province", province);
                                locationData.put("city", city);
                                locationData.put("accuracy", "city");

                                log.info("IP定位通过地理编码成功: {} {}, lng={}, lat={}",
                                    province, city, geoData.get("lng"), geoData.get("lat"));

                                return Map.of(
                                    "code", "200",
                                    "message", "IP定位成功",
                                    "data", locationData
                                );
                            }
                        }
                    } catch (Exception e) {
                        log.warn("地理编码降级失败: {}", e.getMessage());
                    }

                    // 地理编码也失败，返回无坐标的省市信息
                    Map<String, Object> locationData = new HashMap<>();
                    locationData.put("province", province);
                    locationData.put("city", city);
                    locationData.put("accuracy", "province");

                    return Map.of(
                        "code", "200",
                        "message", "IP定位成功（仅省市）",
                        "data", locationData
                    );
                }
            } else {
                String info = jsonNode.path("info").asText();
                String infocode = jsonNode.path("infocode").asText();
                log.error("高德IP定位API返回错误 - status: {}, info: {}, infocode: {}",
                    jsonNode.path("status").asText(), info, infocode);

                String detail = String.format("IP定位失败: %s (%s)",
                    info == null || info.isEmpty() ? "未知错误" : info,
                    infocode == null || infocode.isEmpty() ? "NO_INFOCODE" : infocode);
                return buildResult("500", detail, null);
            }

            String status = jsonNode.path("status").asText("");
            String info = jsonNode.path("info").asText("");
            String infocode = jsonNode.path("infocode").asText("");
            String province = jsonNode.path("province").asText("");
            String city = jsonNode.path("city").asText("");
            String location = jsonNode.path("location").asText("");

            String detail = String.format(
                "IP定位失败: 未获取到有效位置数据 [status=%s, info=%s, infocode=%s, province=%s, city=%s, location=%s]",
                status, info, infocode, province, city, location
            );
            log.error(detail);
            return buildResult("500", detail, null);
        } catch (Exception e) {
            log.error("高德地图IP定位异常", e);
            return buildResult("500", "IP定位异常: " + e.getMessage(), null);
        }
    }
}
