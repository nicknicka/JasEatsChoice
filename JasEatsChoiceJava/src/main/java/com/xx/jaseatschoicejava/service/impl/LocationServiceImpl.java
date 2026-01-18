package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 定位服务实现
 * 使用模拟数据替代高德地图API，避免消耗测试token
 */
@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Override
    public Map<String, Object> getCurrentLocation(Double latitude, Double longitude) {
        // 暂时使用模拟数据，避免消耗高德地图API token
        logger.info("使用模拟定位数据，避免消耗高德地图API token");

        Map<String, Object> location = new HashMap<>();

        // 如果前端传入了经纬度，使用传入的值，否则使用默认值
        Double finalLatitude = (latitude != null) ? latitude : 39.9042;
        Double finalLongitude = (longitude != null) ? longitude : 116.4074;

        location.put("province", "北京市");
        location.put("city", "北京市");
        location.put("district", "朝阳区");
        location.put("address", "北京市北京市朝阳区建国门外大街1号");
        location.put("longitude", finalLongitude.toString());
        location.put("latitude", finalLatitude.toString());

        return location;

        /* 以下是原高德地图API调用代码，已注释
        // 如果前端传入了经纬度，使用逆地理编码获取定位信息
        if (latitude != null && longitude != null) {
            try {
                // 调用高德地图逆地理编码API获取位置信息
                String url = String.format("%s/geocode/regeo?location=%f,%f&key=%s", gaodeApiUrl, longitude, latitude, gaodeApiKey);

                // 从API获取原始数据
                String response = restTemplate.getForObject(url, String.class);

                logger.info("高德地图逆地理编码返回的response = {}", response);

                // 使用Jackson解析JSON
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> responseMap = mapper.readValue(response, Map.class);

                // 解析定位数据
                boolean success = responseMap != null && "1".equals(responseMap.get("status"));

                if (success) {
                    Map<String, Object> location = new HashMap<>();

                    // 解析逆地理编码结果
                    Map<String, Object> regeocode = (Map<String, Object>) responseMap.get("regeocode");
                    if (regeocode != null) {
                        String formattedAddress = (String) regeocode.get("formatted_address");
                        location.put("address", formattedAddress);

                        // 解析地址组件
                        Map<String, Object> addressComponent = (Map<String, Object>) regeocode.get("addressComponent");
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

                    // 返回定位数据
                    return location;
                }
            } catch (Exception e) {
                logger.error("从高德地图API获取逆地理编码数据失败: {}", e.getMessage());
            }
        }
        // 如果前端没有传入经纬度或者逆地理编码失败，回退到IP定位
        try {
            // 调用高德地图IP定位API获取当前位置
            String url = String.format("%s/ip?key=%s", gaodeApiUrl, gaodeApiKey);

            // 从API获取原始数据
            String response = restTemplate.getForObject(url, String.class);

            logger.info("高德地图getCurrentLocation返回的response =  {}", response);
            // 使用Jackson解析JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);


            // 解析定位数据
            boolean success = responseMap != null && "1".equals(responseMap.get("status"));

            if (success) {
                Map<String, Object> location = new HashMap<>();
                // 先初始化城市和区字段为null，后面会覆盖处理
                // 解析经纬度坐标
                Object rectangle = responseMap.get("rectangle");
                String ipLongitude = null;
                String ipLatitude = null;

                if (rectangle != null) {
                    try {
                        String rectangleStr = rectangle.toString();
                        // 处理格式："longitude1,latitude1;longitude2,latitude2"
                        String[] points = rectangleStr.split(";");
                        if (points.length > 0) {
                            String[] coords = points[0].split(",");
                            if (coords.length == 2) {
                                ipLongitude = coords[0];
                                ipLatitude = coords[1];
                            }
                        }
                    } catch (Exception e) {
                        logger.error("解析rectangle字段失败: {}", e.getMessage());
                    }
                }

                location.put("longitude", ipLongitude);
                location.put("latitude", ipLatitude);
                // 处理省份、城市、区字段：如果是数组且不为空则取第一个元素，否则取空字符串
                String province = "";
                if (responseMap.get("province") instanceof List) {
                    List<?> provinceList = (List<?>) responseMap.get("province");
                    province = provinceList.isEmpty() ? "" : provinceList.get(0).toString();
                } else if (responseMap.get("province") != null) {
                    province = responseMap.get("province").toString();
                }

                String city = "";
                if (responseMap.get("city") instanceof List) {
                    List<?> cityList = (List<?>) responseMap.get("city");
                    city = cityList.isEmpty() ? "" : cityList.get(0).toString();
                } else if (responseMap.get("city") != null) {
                    city = responseMap.get("city").toString();
                }

                String district = "";
                if (responseMap.get("district") instanceof List) {
                    List<?> districtList = (List<?>) responseMap.get("district");
                    district = districtList.isEmpty() ? "" : districtList.get(0).toString();
                } else if (responseMap.get("district") != null) {
                    district = responseMap.get("district").toString();
                }

                location.put("province", province);
                location.put("city", city);
                location.put("district", district);

                location.put("address", province + city + district);

                // 如果解析结果有效，返回真实数据，否则返回空数据
                if (!city.isEmpty() || longitude != null) {
                    return location;
                } else {
                    logger.warn("从高德地图API获取有效的定位数据失败: 所有字段都是null或空值");
                    return new HashMap<>();
                }
            }
        } catch (Exception e) {
            logger.error("从高德地图API获取真实定位数据失败: {}", e.getMessage());
            e.printStackTrace();
            logger.warn("由于API获取失败，返回空的定位数据");
        }

        // API调用失败或未获取到有效数据时返回空数据
        return new HashMap<>();
        */
    }

    @Override
    public List<Map<String, Object>> getCascaderLocationData() {
        // 暂时使用模拟数据，避免消耗高德地图API token
        logger.info("使用模拟省市区数据，避免消耗高德地图API token");

        List<Map<String, Object>> cascaderData = new ArrayList<>();

        // 模拟北京市数据
        Map<String, Object> beijing = new HashMap<>();
        beijing.put("value", "北京市");
        beijing.put("label", "北京市");

        List<Map<String, Object>> beijingAreas = new ArrayList<>();

        Map<String, Object> chaoyang = new HashMap<>();
        chaoyang.put("value", "朝阳区");
        chaoyang.put("label", "朝阳区");

        Map<String, Object> haidian = new HashMap<>();
        haidian.put("value", "海淀区");
        haidian.put("label", "海淀区");

        Map<String, Object> dongcheng = new HashMap<>();
        dongcheng.put("value", "东城区");
        dongcheng.put("label", "东城区");

        Map<String, Object> xicheng = new HashMap<>();
        xicheng.put("value", "西城区");
        xicheng.put("label", "西城区");

        beijingAreas.add(chaoyang);
        beijingAreas.add(haidian);
        beijingAreas.add(dongcheng);
        beijingAreas.add(xicheng);

        beijing.put("children", beijingAreas);
        cascaderData.add(beijing);

        // 模拟上海市数据
        Map<String, Object> shanghai = new HashMap<>();
        shanghai.put("value", "上海市");
        shanghai.put("label", "上海市");

        List<Map<String, Object>> shanghaiAreas = new ArrayList<>();

        Map<String, Object> huangpu = new HashMap<>();
        huangpu.put("value", "黄浦区");
        huangpu.put("label", "黄浦区");

        Map<String, Object> xuhui = new HashMap<>();
        xuhui.put("value", "徐汇区");
        xuhui.put("label", "徐汇区");

        Map<String, Object> changning = new HashMap<>();
        changning.put("value", "长宁区");
        changning.put("label", "长宁区");

        Map<String, Object> pudong = new HashMap<>();
        pudong.put("value", "浦东新区");
        pudong.put("label", "浦东新区");

        shanghaiAreas.add(huangpu);
        shanghaiAreas.add(xuhui);
        shanghaiAreas.add(changning);
        shanghaiAreas.add(pudong);

        shanghai.put("children", shanghaiAreas);
        cascaderData.add(shanghai);

        // 模拟广东省数据
        Map<String, Object> guangdong = new HashMap<>();
        guangdong.put("value", "广东省");
        guangdong.put("label", "广东省");

        List<Map<String, Object>> guangdongCities = new ArrayList<>();

        Map<String, Object> guangzhou = new HashMap<>();
        guangzhou.put("value", "广州市");
        guangzhou.put("label", "广州市");

        List<Map<String, Object>> guangzhouAreas = new ArrayList<>();
        Map<String, Object> tianhe = new HashMap<>();
        tianhe.put("value", "天河区");
        tianhe.put("label", "天河区");
        Map<String, Object> yuexiu = new HashMap<>();
        yuexiu.put("value", "越秀区");
        yuexiu.put("label", "越秀区");
        guangzhouAreas.add(tianhe);
        guangzhouAreas.add(yuexiu);
        guangzhou.put("children", guangzhouAreas);

        Map<String, Object> shenzhen = new HashMap<>();
        shenzhen.put("value", "深圳市");
        shenzhen.put("label", "深圳市");

        List<Map<String, Object>> shenzhenAreas = new ArrayList<>();
        Map<String, Object> futian = new HashMap<>();
        futian.put("value", "福田区");
        futian.put("label", "福田区");
        Map<String, Object> nanshan = new HashMap<>();
        nanshan.put("value", "南山区");
        nanshan.put("label", "南山区");
        shenzhenAreas.add(futian);
        shenzhenAreas.add(nanshan);
        shenzhen.put("children", shenzhenAreas);

        guangdongCities.add(guangzhou);
        guangdongCities.add(shenzhen);

        guangdong.put("children", guangdongCities);
        cascaderData.add(guangdong);

        return cascaderData;

        /* 以下是原高德地图API调用代码，已注释
        try {
            // 调用高德地图API获取中国所有省市区数据，subdistrict=3表示获取到区县级别
            String url = String.format("%s/config/district?keywords=中国&subdistrict=3&key=%s", gaodeApiUrl, gaodeApiKey);

            // 从API获取原始数据
            String response = restTemplate.getForObject(url, String.class);
//            logger.info("高德地图返回的response =  {}", response);
            // 使用Jackson解析JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);

            // 解析省份数据
            List<Map<String, Object>> cascaderData = new ArrayList<>();

            boolean success = responseMap != null && "1".equals(responseMap.get("status"));

            if (success) {
                List<Map<String, Object>> districts = (List<Map<String, Object>>) responseMap.get("districts");

                if (districts != null && !districts.isEmpty()) {
                    Map<String, Object> china = districts.get(0);
                    List<Map<String, Object>> provinces = (List<Map<String, Object>>) china.get("districts");

                    for (Map<String, Object> province : provinces) {
                        // 创建省份节点
                        Map<String, Object> provinceNode = new HashMap<>();
                        provinceNode.put("value", province.get("name"));
                        provinceNode.put("label", province.get("name"));

                        // 获取城市数据
                        List<Map<String, Object>> cities = (List<Map<String, Object>>) province.get("districts");
                        List<Map<String, Object>> cityNodes = new ArrayList<>();

                        for (Map<String, Object> city : cities) {
                            // 创建城市节点
                            Map<String, Object> cityNode = new HashMap<>();
                            cityNode.put("value", city.get("name"));
                            cityNode.put("label", city.get("name"));

                            // 获取区域数据
                            List<Map<String, Object>> areas = (List<Map<String, Object>>) city.get("districts");
                            List<Map<String, Object>> areaNodes = new ArrayList<>();

                            for (Map<String, Object> area : areas) {
                                // 创建区域节点
                                Map<String, Object> areaNode = new HashMap<>();
                                areaNode.put("value", area.get("name"));
                                areaNode.put("label", area.get("name"));

                                areaNodes.add(areaNode);
                            }

                            cityNode.put("children", areaNodes);
                            cityNodes.add(cityNode);
                        }

                        provinceNode.put("children", cityNodes);
                        cascaderData.add(provinceNode);
                    }
                }

                if (cascaderData.isEmpty()) {
                    // 如果解析结果为空，返回空数据并记录日志
                    logger.warn("从高德地图API获取并解析有效的位置数据失败: 解析结果为空数据");
                    return new ArrayList<>();
                }
            } else {
                // 如果API调用成功但返回错误状态，返回空数据并记录日志
                logger.warn("从高德地图API获取位置数据失败: API返回错误状态");
                return new ArrayList<>();
            }

            return cascaderData;
        } catch (Exception e) {
            logger.error("从高德地图API获取位置数据失败: {}", e.getMessage());
            e.printStackTrace();
            logger.warn("由于API获取失败，返回空的位置数据");
            return new ArrayList<>();
        }
        */
    }

    @Override
    public List<Map<String, Object>> searchAddress(String keyword) {
        // 暂时使用模拟数据，避免消耗高德地图API token
        logger.info("使用模拟地址搜索数据，避免消耗高德地图API token，搜索关键词: {}", keyword);

        List<Map<String, Object>> searchResults = new ArrayList<>();

        // 根据关键词返回不同的模拟数据
        if (keyword != null && !keyword.isEmpty()) {
            // 模拟一些北京的地址
            Map<String, Object> address1 = new HashMap<>();
            address1.put("id", "B000A7BD6C");
            address1.put("name", "建国门外大街1号");
            address1.put("address", "建国门外大街1号");
            address1.put("adname", "朝阳区");
            address1.put("cityname", "北京市");
            address1.put("pname", "北京市");
            address1.put("longitude", 116.4074);
            address1.put("latitude", 39.9042);
            searchResults.add(address1);

            Map<String, Object> address2 = new HashMap<>();
            address2.put("id", "B000A85M3Q");
            address2.put("name", "国贸大厦");
            address2.put("address", "建国门外大街1号国贸大厦");
            address2.put("adname", "朝阳区");
            address2.put("cityname", "北京市");
            address2.put("pname", "北京市");
            address2.put("longitude", 116.4581);
            address2.put("latitude", 39.9108);
            searchResults.add(address2);

            Map<String, Object> address3 = new HashMap<>();
            address3.put("id", "B000A85M3P");
            address3.put("name", "中关村软件园");
            address3.put("address", "东北旺西路8号中关村软件园");
            address3.put("adname", "海淀区");
            address3.put("cityname", "北京市");
            address3.put("pname", "北京市");
            address3.put("longitude", 116.2856);
            address3.put("latitude", 40.0495);
            searchResults.add(address3);

            Map<String, Object> address4 = new HashMap<>();
            address4.put("id", "B000A85M3N");
            address4.put("name", "王府井大街");
            address4.put("address", "王府井大街");
            address4.put("adname", "东城区");
            address4.put("cityname", "北京市");
            address4.put("pname", "北京市");
            address4.put("longitude", 116.4105);
            address4.put("latitude", 39.9129);
            searchResults.add(address4);

            Map<String, Object> address5 = new HashMap<>();
            address5.put("id", "B000A85M3M");
            address5.put("name", "三里屯SOHO");
            address5.put("address", "三里屯路11号院");
            address5.put("adname", "朝阳区");
            address5.put("cityname", "北京市");
            address5.put("pname", "北京市");
            address5.put("longitude", 116.4556);
            address5.put("latitude", 39.9382);
            searchResults.add(address5);
        }

        return searchResults;

        /* 以下是原高德地图API调用代码，已注释
        try {
            // 调用高德地图POI搜索API
            String url = String.format("%s/place/text?keywords=%s&key=%s&city=&offset=20", gaodeApiUrl, keyword, gaodeApiKey);

            // 从API获取原始数据
            String response = restTemplate.getForObject(url, String.class);

            logger.info("高德地图地址搜索返回的response = {}", response);

            // 使用Jackson解析JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);

            // 解析搜索结果
            boolean success = responseMap != null && "1".equals(responseMap.get("status"));

            if (success) {
                List<Map<String, Object>> pois = (List<Map<String, Object>>) responseMap.get("pois");
                List<Map<String, Object>> searchResults = new ArrayList<>();

                if (pois != null && !pois.isEmpty()) {
                    for (Map<String, Object> poi : pois) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("id", poi.get("id"));
                        result.put("name", poi.get("name"));
                        result.put("address", poi.get("address"));
                        result.put("adname", poi.get("adname"));
                        result.put("cityname", poi.get("cityname"));
                        result.put("pname", poi.get("pname"));

                        // 解析经纬度坐标
                        String locationStr = (String) poi.get("location");
                        if (locationStr != null && !locationStr.isEmpty()) {
                            String[] coords = locationStr.split(",");
                            if (coords.length == 2) {
                                try {
                                    result.put("longitude", Double.parseDouble(coords[0]));
                                    result.put("latitude", Double.parseDouble(coords[1]));
                                } catch (NumberFormatException e) {
                                    logger.error("解析坐标失败: {}", locationStr);
                                }
                            }
                        }

                        searchResults.add(result);
                    }
                }

                return searchResults;
            }
        } catch (Exception e) {
            logger.error("从高德地图API搜索地址失败: {}", e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
        */
    }

}
