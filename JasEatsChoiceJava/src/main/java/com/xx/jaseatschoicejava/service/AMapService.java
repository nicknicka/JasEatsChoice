package com.xx.jaseatschoicejava.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.config.AMapConfig;
import com.xx.jaseatschoicejava.service.dto.AmapApiResponse;
import com.xx.jaseatschoicejava.service.dto.AmapLocationData;
import com.xx.jaseatschoicejava.service.dto.AmapPoiData;
import com.xx.jaseatschoicejava.service.dto.AmapPoiLocation;

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
    public AmapApiResponse<List<AmapPoiData>> searchAddress(String keywords, String city) {
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
                List<AmapPoiData> searchResults = new ArrayList<>();

                for (JsonNode poi : pois) {
                    AmapPoiLocation location = null;
                    String locationStr = poi.path("location").asText();
                    if (locationStr != null && !locationStr.isEmpty()) {
                        String[] coords = locationStr.split(",");
                        if (coords.length == 2) {
                            try {
                                double lng = Double.parseDouble(coords[0].trim());
                                double lat = Double.parseDouble(coords[1].trim());
                                location = new AmapPoiLocation(lng, lat);
                            } catch (NumberFormatException e) {
                                log.warn("解析坐标失败: {}", locationStr);
                            }
                        }
                    }

                    searchResults.add(new AmapPoiData(
                        poi.path("name").asText(),
                        poi.path("address").asText(""),
                        poi.path("pname").asText(""),
                        location
                    ));
                }

                return new AmapApiResponse<>("200", "搜索成功", searchResults);
            } else {
                // 失败
                String info = jsonNode.path("info").asText();
                String infocode = jsonNode.path("infocode").asText();
                log.error("高德地图搜索失败 - info: {}, infocode: {}", info, infocode);

                return new AmapApiResponse<>("500", "搜索失败: " + info, List.of());
            }
        } catch (RestClientException | JsonProcessingException e) {
            log.error("高德地图搜索异常", e);
            return new AmapApiResponse<>("500", "搜索异常: " + e.getMessage(), List.of());
        }
    }

    /**
     * 行政区划数据（省市区）
     */
    public AmapApiResponse<List<Map<String, Object>>> getDistrictData(String keywords, Integer subdistrict) {
        try {
            String url = "https://restapi.amap.com/v3/config/district";
            String apiKey = aMapConfig.getApiKey();
            Integer finalSubdistrict = subdistrict != null ? subdistrict : 3;

            ResponseEntity<String> response = restTemplate.getForEntity(
                url + "?key={key}&keywords={keywords}&subdistrict={subdistrict}&extensions=base&output=JSON",
                String.class,
                apiKey,
                keywords,
                finalSubdistrict
            );

            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                return new AmapApiResponse<>("500", "行政区数据响应为空", List.of());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);

            if (!"1".equals(jsonNode.path("status").asText())) {
                String info = jsonNode.path("info").asText();
                return new AmapApiResponse<>("500", "行政区数据获取失败: " + info, List.of());
            }

            JsonNode districts = jsonNode.path("districts");
            if (districts == null || !districts.isArray() || districts.isEmpty()) {
                return new AmapApiResponse<>("404", "未找到行政区数据", List.of());
            }

            JsonNode china = districts.get(0);
            JsonNode provinceNodes = china.path("districts");
            List<Map<String, Object>> cascaderData = convertDistrictNodes(provinceNodes, 1);
            return new AmapApiResponse<>("200", "行政区数据获取成功", cascaderData);
        } catch (RestClientException | JsonProcessingException e) {
            log.error("高德地图行政区数据获取异常", e);
            return new AmapApiResponse<>("500", "行政区数据获取异常: " + e.getMessage(), List.of());
        }
    }

    /**
     * 地理编码（地址 → 坐标）
     */
    public AmapApiResponse<AmapLocationData> geocode(String address, String city) {
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
        } catch (RestClientException e) {
            log.error("高德地图地理编码异常", e);
            return new AmapApiResponse<>("500", "地理编码异常: " + e.getMessage(), null);
        }
    }

    /**
     * 解析地理编码响应
     */
    private AmapApiResponse<AmapLocationData> parseGeocodeResponse(String responseBody) {
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

                            return new AmapApiResponse<>(
                                "200",
                                "地理编码成功",
                                new AmapLocationData(
                                    Double.valueOf(lng),
                                    Double.valueOf(lat),
                                    null,
                                    null,
                                    null,
                                    geocode.path("formatted_address").asText("")
                                )
                            );
                        }
                    }

                    // location 字段解析失败，返回明确错误信息，便于排查第三方接口格式变化
                    log.warn("地理编码返回location字段格式异常: {}", locationStr);
                    return new AmapApiResponse<>("500", "地理编码返回坐标格式异常", null);
                }
            }

            return new AmapApiResponse<>("404", "未找到该地址", null);
        } catch (RestClientException | JsonProcessingException e) {
            log.error("解析地理编码响应异常", e);
            return new AmapApiResponse<>("500", "解析响应失败", null);
        }
    }

    /**
     * 逆地理编码（坐标 → 地址）
     */
    public AmapApiResponse<AmapLocationData> regeocode(String lng, String lat) {
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
                    return new AmapApiResponse<>(
                        "200",
                        "逆地理编码成功",
                        new AmapLocationData(
                            null,
                            null,
                            null,
                            null,
                            null,
                            regeocode.path("formatted_address").asText()
                        )
                    );
                }
            }

            return new AmapApiResponse<>("404", "未找到该位置", null);
        } catch (RestClientException | JsonProcessingException e) {
            log.error("高德地图逆地理编码异常", e);
            return new AmapApiResponse<>("500", "逆地理编码异常: " + e.getMessage(), null);
        }
    }

    /**
     * IP 定位（通过 IP 地址获取大概位置）
     * 文档：https://lbs.amap.com/api/webservice/guide/api/ipconfig
     * 精度：城市级别（比 GPS 精确度低，但不需要用户授权）
     */
    public AmapApiResponse<AmapLocationData> ipLocation() {
        try {
            String url = "https://restapi.amap.com/v3/ip";
            String apiKey = aMapConfig.getApiKey();

            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("高德IP定位失败: amap.api-key 未配置");
                return new AmapApiResponse<>("500", "IP定位失败: amap.api-key 未配置", null);
            }

            ResponseEntity<String> response = restTemplate.getForEntity(
                url + "?key={key}",
                String.class,
                apiKey
            );

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                log.error("高德IP定位失败: 响应体为空");
                return new AmapApiResponse<>("500", "IP定位失败: 高德接口响应为空", null);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);

            log.debug("高德IP定位原始响应: {}", responseBody);

            if ("1".equals(jsonNode.path("status").asText())) {
                String province = extractFirstText(jsonNode.path("province"));
                String city = extractFirstText(jsonNode.path("city"));

                double[] coords = parseIpCoordinates(jsonNode);
                if (coords != null) {
                    log.info("IP定位成功: {} {}, lng={}, lat={}", province, city, coords[0], coords[1]);

                    return new AmapApiResponse<>(
                        "200",
                        "IP定位成功",
                        new AmapLocationData(
                            coords[0],
                            coords[1],
                            province,
                            city,
                            "city",
                            null
                        )
                    );
                }

                // 如果没有坐标，尝试通过城市名做地理编码获取坐标
                if (!province.isEmpty() || !city.isEmpty()) {
                    String address = city.isEmpty() ? province : city;
                    log.info("IP定位无坐标，尝试地理编码: {}", address);

                    try {
                        AmapApiResponse<AmapLocationData> geocodeResult = geocode(address, city);
                        if (geocodeResult.isSuccess() && geocodeResult.data() != null) {
                            AmapLocationData geoData = geocodeResult.data();
                            if (geoData.lng() != null && geoData.lat() != null) {
                                log.info("IP定位通过地理编码成功: {} {}, lng={}, lat={}",
                                    province, city, geoData.lng(), geoData.lat());

                                return new AmapApiResponse<>(
                                    "200",
                                    "IP定位成功",
                                    new AmapLocationData(
                                        geoData.lng(),
                                        geoData.lat(),
                                        province,
                                        city,
                                        "city",
                                        null
                                    )
                                );
                            }
                        }
                    } catch (Exception e) {
                        log.warn("地理编码降级失败: {}", e.getMessage());
                    }

                    // 地理编码也失败，返回无坐标的省市信息
                    return new AmapApiResponse<>(
                        "200",
                        "IP定位成功（仅省市）",
                        new AmapLocationData(
                            null,
                            null,
                            province,
                            city,
                            "province",
                            null
                        )
                    );
                }

                // 高德返回成功但无可用字段时，不再硬失败，交给前端继续后续定位链路
                log.warn("IP定位返回成功但无可用位置信息: {}", responseBody);
                return new AmapApiResponse<>("200", "IP定位暂无有效位置信息", null);
            } else {
                String info = jsonNode.path("info").asText();
                String infocode = jsonNode.path("infocode").asText();
                log.error("高德IP定位API返回错误 - status: {}, info: {}, infocode: {}",
                    jsonNode.path("status").asText(), info, infocode);

                String detail = String.format("IP定位失败: %s (%s)",
                    info == null || info.isEmpty() ? "未知错误" : info,
                    infocode == null || infocode.isEmpty() ? "NO_INFOCODE" : infocode);
                return new AmapApiResponse<>("500", detail, null);
            }
        } catch (RestClientException | JsonProcessingException e) {
            log.error("高德地图IP定位异常", e);
            return new AmapApiResponse<>("500", "IP定位异常: " + e.getMessage(), null);
        }
    }

    private String extractFirstText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        if (node.isArray()) {
            if (node.size() == 0) {
                return "";
            }
            JsonNode first = node.get(0);
            return first == null || first.isNull() ? "" : first.asText("").trim();
        }
        return node.asText("").trim();
    }

    private double[] parseIpCoordinates(JsonNode root) {
        String location = extractFirstText(root.path("location"));
        double[] fromLocation = parseLngLatText(location);
        if (fromLocation != null) {
            return fromLocation;
        }

        // v3/ip 常见返回 rectangle: "lng1,lat1;lng2,lat2"
        String rectangle = extractFirstText(root.path("rectangle"));
        if (rectangle.isEmpty()) {
            return null;
        }

        String[] points = rectangle.split(";");
        if (points.length == 0) {
            return null;
        }

        double[] p1 = parseLngLatText(points[0]);
        if (p1 == null) {
            return null;
        }

        if (points.length == 1) {
            return p1;
        }

        double[] p2 = parseLngLatText(points[1]);
        if (p2 == null) {
            return p1;
        }

        return new double[] { (p1[0] + p2[0]) / 2.0, (p1[1] + p2[1]) / 2.0 };
    }

    private double[] parseLngLatText(String lngLatText) {
        if (lngLatText == null || lngLatText.trim().isEmpty()) {
            return null;
        }
        String[] parts = lngLatText.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            double lng = Double.parseDouble(parts[0].trim());
            double lat = Double.parseDouble(parts[1].trim());
            return new double[] { lng, lat };
        } catch (NumberFormatException e) {
            log.warn("解析经纬度失败: {}", lngLatText);
            return null;
        }
    }

    private List<Map<String, Object>> convertDistrictNodes(JsonNode districts, int level) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        if (districts == null || !districts.isArray()) {
            return nodes;
        }

        for (JsonNode district : districts) {
            Map<String, Object> node = new HashMap<>();
            String name = district.path("name").asText("");
            node.put("value", name);
            node.put("label", name);

            JsonNode children = district.path("districts");
            if (children != null && children.isArray() && children.size() > 0 && level < 3) {
                node.put("children", convertDistrictNodes(children, level + 1));
            }

            nodes.add(node);
        }

        return nodes;
    }
}
