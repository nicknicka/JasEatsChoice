package com.xx.jaseatschoicejava.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.service.AMapService;
import com.xx.jaseatschoicejava.service.LocationService;
import com.xx.jaseatschoicejava.service.dto.AmapApiResponse;
import com.xx.jaseatschoicejava.service.dto.AmapLocationData;
import com.xx.jaseatschoicejava.service.dto.AmapPoiData;

/**
 * 定位服务实现
 * 统一通过 Location 入口提供定位能力，底层复用高德地图 API
 */
@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gaode.map.api.key}")
    private String gaodeApiKey;

    @Value("${gaode.map.api.url}")
    private String gaodeApiUrl;

    @Value("${tencent.map.api.key:}")
    private String tencentApiKey;

    @Value("${tencent.map.api.url:https://apis.map.qq.com}")
    private String tencentApiUrl;

    private final AMapService aMapService;

    public LocationServiceImpl(AMapService aMapService) {
        this.aMapService = aMapService;
    }

    public Map<String, Object> getCurrentLocation(Double latitude, Double longitude) {
        return getCurrentLocation(latitude, longitude, null);
    }

    @Override
    public Map<String, Object> getCurrentLocation(Double latitude, Double longitude, String clientIp) {
        // 如果前端传入了经纬度，使用逆地理编码获取定位信息
        if (latitude != null && longitude != null) {
            try {
                String url = String.format("%s/geocode/regeo?location=%f,%f&key=%s", gaodeApiUrl, longitude, latitude, gaodeApiKey);
                ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
                String response = responseEntity.getBody();
                if (response == null || response.isEmpty()) {
                    return new HashMap<>();
                }

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});

                boolean success = "1".equals(responseMap.get("status"));
                if (success) {
                    Map<String, Object> location = new HashMap<>();

                    Map<String, Object> regeocode = asMap(responseMap.get("regeocode"));
                    if (regeocode != null) {
                        String formattedAddress = (String) regeocode.get("formatted_address");
                        location.put("address", formattedAddress);

                        Map<String, Object> addressComponent = asMap(regeocode.get("addressComponent"));
                        if (addressComponent != null) {
                            String province = (String) addressComponent.get("province");
                            String city = (String) addressComponent.get("city");
                            String district = (String) addressComponent.get("district");

                            location.put("province", province != null ? province : "");
                            location.put("city", city != null ? city : "");
                            location.put("district", district != null ? district : "");
                        }
                    }

                    location.put("longitude", longitude.toString());
                    location.put("latitude", latitude.toString());
                    return location;
                }
            } catch (RestClientException | java.io.IOException e) {
                logger.error("从高德地图API获取逆地理编码数据失败: {}", e.getMessage());
            }
        }

        // 如果前端没有传入经纬度或者逆地理编码失败，回退到腾讯IP定位
        Map<String, Object> tencentLocation = getLocationByTencentIp(clientIp);
        if (!tencentLocation.isEmpty()) {
            return tencentLocation;
        }

        logger.warn("腾讯IP定位未返回有效结果，返回空定位数据供前端继续降级处理");
        return new HashMap<>();
    }

    private Map<String, Object> getLocationByTencentIp(String clientIp) {
        Map<String, Object> emptyLocation = new HashMap<>();

        if (!hasText(tencentApiKey)) {
            logger.warn("腾讯IP定位失败: tencent.map.api.key 未配置");
            return emptyLocation;
        }

        try {
            StringBuilder urlBuilder = new StringBuilder(tencentApiUrl)
                .append("/ws/location/v1/ip?key=")
                .append(URLEncoder.encode(tencentApiKey.trim(), StandardCharsets.UTF_8));

            if (hasText(clientIp)) {
                urlBuilder.append("&ip=")
                    .append(URLEncoder.encode(clientIp.trim(), StandardCharsets.UTF_8));
            }

            String url = urlBuilder.toString();
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            String response = responseEntity.getBody();
            if (response == null || response.isEmpty()) {
                logger.warn("腾讯IP定位失败: 响应体为空");
                return emptyLocation;
            }

            logger.info("腾讯IP接口原始响应: {}", response);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});
            if (responseMap == null) {
                logger.warn("腾讯IP定位失败: 响应解析为空");
                return emptyLocation;
            }

            String status = extractTextField(responseMap.get("status"));
            String message = extractTextField(responseMap.get("message"));
            Map<String, Object> result = asMap(responseMap.get("result"));
            Map<String, Object> resultLocation = result != null ? asMap(result.get("location")) : null;
            Map<String, Object> adInfo = result != null ? asMap(result.get("ad_info")) : null;

            String province = adInfo != null ? extractTextField(adInfo.get("province")) : "";
            String city = adInfo != null ? extractTextField(adInfo.get("city")) : "";
            String district = adInfo != null ? extractTextField(adInfo.get("district")) : "";
            String address = adInfo != null ? extractTextField(adInfo.get("nation")) + province + city + district : "";

            logger.info(
                "腾讯IP定位响应: clientIp={}, status={}, message={}, province={}, city={}, district={}, address={}",
                hasText(clientIp) ? clientIp.trim() : "",
                status,
                message,
                province,
                city,
                district,
                address
            );

            boolean success = "0".equals(status);
            if (success) {
                Map<String, Object> locationResult = new HashMap<>();
                locationResult.put("province", province);
                locationResult.put("city", city);
                locationResult.put("district", district);

                String resolvedAddress = hasText(address) ? address : province + city + district;
                locationResult.put("address", resolvedAddress);

                Map<String, Object> geocodedLocation = geocodeLocationFromTencentIp(province, city, district);
                if (!geocodedLocation.isEmpty()) {
                    locationResult.putAll(geocodedLocation);
                    return locationResult;
                }

                String pointLongitude = extractTextField(resultLocation != null ? resultLocation.get("lng") : null);
                String pointLatitude = extractTextField(resultLocation != null ? resultLocation.get("lat") : null);
                locationResult.put("longitude", hasText(pointLongitude) ? pointLongitude : null);
                locationResult.put("latitude", hasText(pointLatitude) ? pointLatitude : null);

                boolean hasRegionData = hasText(province) || hasText(city) || hasText(district);
                boolean hasCoordinateData = hasText(pointLongitude) && hasText(pointLatitude);

                if (hasRegionData || hasCoordinateData) {
                    return locationResult;
                }

                logger.info("腾讯IP定位返回status=0但区域与坐标字段为空，返回空定位结果供前端降级处理");
                return locationResult;
            }

            logger.warn("腾讯IP定位返回非成功状态: clientIp={}, status={}, message={}",
                hasText(clientIp) ? clientIp.trim() : "", status, message);
        } catch (java.io.IOException e) {
            logger.error("从腾讯地图API解析定位数据失败: {}", e.getMessage());
            logger.warn("由于API解析失败，返回空的定位数据");
        } catch (RuntimeException e) {
            logger.error("从腾讯地图API获取真实定位数据失败: {}", e.getMessage());
            logger.warn("由于API获取失败，返回空的定位数据");
        }

        return emptyLocation;
    }

    private Map<String, Object> geocodeLocationFromTencentIp(String province, String city, String district) {
        String geocodeCity = hasText(city) ? city : province;
        String geocodeAddress = hasText(district) ? district : geocodeCity;

        if (!hasText(geocodeAddress)) {
            return new HashMap<>();
        }

        try {
            AmapApiResponse<AmapLocationData> geocodeResult = aMapService.geocode(geocodeAddress, geocodeCity);
            if (geocodeResult != null && geocodeResult.isSuccess() && geocodeResult.data() != null) {
                AmapLocationData data = geocodeResult.data();
                if (data.lng() != null && data.lat() != null) {
                    Map<String, Object> location = new HashMap<>();
                    location.put("longitude", data.lng().toString());
                    location.put("latitude", data.lat().toString());
                    if (hasText(data.formattedAddress())) {
                        location.put("address", data.formattedAddress());
                    }
                    return location;
                }
            }
        } catch (RuntimeException e) {
            logger.warn("腾讯IP定位后的高德地理编码失败: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            return (Map<String, Object>) mapValue;
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String extractTextField(Object field) {
        if (field instanceof List<?> list) {
            if (list.isEmpty() || list.get(0) == null) {
                return "";
            }
            String value = list.get(0).toString().trim();
            return value;
        }

        if (field == null) {
            return "";
        }

        return field.toString().trim();
    }

    @Override
    public Map<String, Object> geocode(String address, String city) {
        AmapApiResponse<AmapLocationData> result = aMapService.geocode(address, city);
        return toLocationMap(result != null ? result.data() : null);
    }

    @Override
    public Map<String, Object> reverseGeocode(String lng, String lat) {
        AmapApiResponse<AmapLocationData> result = aMapService.reverseGeocode(lng, lat);
        return toLocationMap(result != null ? result.data() : null);
    }

    @Override
    public List<Map<String, Object>> getCascaderLocationData() {
        AmapApiResponse<List<Map<String, Object>>> result = aMapService.getDistrictData("中国", 3);
        if (result != null && result.isSuccess() && result.data() != null && !result.data().isEmpty()) {
            return result.data();
        }

        logger.warn("高德行政区数据获取失败，返回空列表");
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> searchAddress(String keyword) {
        AmapApiResponse<List<AmapPoiData>> result = aMapService.searchAddress(keyword, null);
        if (result != null && result.isSuccess() && result.data() != null) {
            return convertPoiResults(result.data());
        }

        logger.warn("高德地址搜索失败，回退到空列表");
        return new ArrayList<>();
    }

    private Map<String, Object> toLocationMap(AmapLocationData data) {
        Map<String, Object> location = new HashMap<>();
        if (data == null) {
            return location;
        }

        if (data.lng() != null) {
            location.put("lng", data.lng());
            location.put("longitude", data.lng().toString());
        }
        if (data.lat() != null) {
            location.put("lat", data.lat());
            location.put("latitude", data.lat().toString());
        }
        location.put("province", data.province() != null ? data.province() : "");
        location.put("city", data.city() != null ? data.city() : "");
        location.put("district", "");
        location.put("address", data.formattedAddress() != null ? data.formattedAddress() : "");
        location.put("formattedAddress", data.formattedAddress() != null ? data.formattedAddress() : "");
        location.put("accuracy", data.accuracy() != null ? data.accuracy() : "");
        return location;
    }

    private List<Map<String, Object>> convertPoiResults(List<AmapPoiData> pois) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (AmapPoiData poi : pois) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", poi.name());
            item.put("address", poi.address());
            item.put("pname", poi.pname());
            item.put("cityname", "");
            item.put("adname", "");

            if (poi.location() != null) {
                item.put("lng", poi.location().lng());
                item.put("lat", poi.location().lat());
                item.put("longitude", poi.location().lng());
                item.put("latitude", poi.location().lat());
                Map<String, Object> location = new HashMap<>();
                location.put("lng", poi.location().lng());
                location.put("lat", poi.location().lat());
                item.put("location", location);
            }

            results.add(item);
        }

        return results;
    }

    /*
     * 原高德地图 API 调用代码（保留参考）
     *
     * 这部分历史实现是可用的，只是当前主链路已经统一到 Location 入口。
     * 如果后续需要直接回退到高德原生接口，可以按下面思路恢复。
     *
     * public Map<String, Object> getCurrentLocation(Double latitude, Double longitude) {
     *     // 如果前端传入了经纬度，使用逆地理编码获取定位信息
     *     if (latitude != null && longitude != null) {
     *         try {
     *             String url = String.format("%s/geocode/regeo?location=%f,%f&key=%s",
     *                 gaodeApiUrl, longitude, latitude, gaodeApiKey);
     *             String response = restTemplate.getForObject(url, String.class);
     *             ObjectMapper mapper = new ObjectMapper();
     *             Map<String, Object> responseMap = mapper.readValue(response, Map.class);
     *             boolean success = responseMap != null && "1".equals(responseMap.get("status"));
     *             if (success) {
     *                 Map<String, Object> location = new HashMap<>();
     *                 Map<String, Object> regeocode = (Map<String, Object>) responseMap.get("regeocode");
     *                 if (regeocode != null) {
     *                     location.put("address", regeocode.get("formatted_address"));
     *                 }
     *                 location.put("longitude", longitude.toString());
     *                 location.put("latitude", latitude.toString());
     *                 return location;
     *             }
     *         } catch (Exception e) {
     *             logger.error("从高德地图API获取逆地理编码数据失败: {}", e.getMessage());
     *         }
     *     }
     *
     *     // 回退到 IP 定位
     *     try {
     *         String url = String.format("%s/ip?key=%s", gaodeApiUrl, gaodeApiKey);
     *         String response = restTemplate.getForObject(url, String.class);
     *         ObjectMapper mapper = new ObjectMapper();
     *         Map<String, Object> responseMap = mapper.readValue(response, Map.class);
     *         boolean success = responseMap != null && "1".equals(responseMap.get("status"));
     *         if (success) {
     *             Map<String, Object> location = new HashMap<>();
     *             Object rectangle = responseMap.get("rectangle");
     *             String ipLongitude = null;
     *             String ipLatitude = null;
     *             if (rectangle != null) {
     *                 String rectangleStr = rectangle.toString();
     *                 String[] points = rectangleStr.split(";");
     *                 if (points.length > 0) {
     *                     String[] coords = points[0].split(",");
     *                     if (coords.length == 2) {
     *                         ipLongitude = coords[0];
     *                         ipLatitude = coords[1];
     *                     }
     *                 }
     *             }
     *             location.put("longitude", ipLongitude);
     *             location.put("latitude", ipLatitude);
     *             location.put("province", responseMap.get("province") != null ? responseMap.get("province").toString() : "");
     *             location.put("city", responseMap.get("city") != null ? responseMap.get("city").toString() : "");
     *             location.put("district", responseMap.get("district") != null ? responseMap.get("district").toString() : "");
     *             location.put("address", location.get("province") + location.get("city") + location.get("district"));
     *             return location;
     *         }
     *     } catch (Exception e) {
     *         logger.error("从高德地图API获取真实定位数据失败: {}", e.getMessage());
     *     }
     *
     *     return new HashMap<>();
     * }
     */

}
