package com.xx.jaseatschoicejava.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    private final AMapService aMapService;

    public LocationServiceImpl(AMapService aMapService) {
        this.aMapService = aMapService;
    }

    @Override
    public Map<String, Object> getCurrentLocation(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            return reverseGeocode(longitude.toString(), latitude.toString());
        }

        AmapApiResponse<AmapLocationData> ipResult = aMapService.ipLocation();
        Map<String, Object> location = toLocationMap(ipResult != null ? ipResult.data() : null);

        if (location.isEmpty()) {
            logger.warn("通过高德IP定位未获取到有效数据，返回空结果");
        }

        return location;
    }

    @Override
    public Map<String, Object> geocode(String address, String city) {
        AmapApiResponse<AmapLocationData> result = aMapService.geocode(address, city);
        return toLocationMap(result != null ? result.data() : null);
    }

    @Override
    public Map<String, Object> reverseGeocode(String lng, String lat) {
        AmapApiResponse<AmapLocationData> result = aMapService.regeocode(lng, lat);
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
