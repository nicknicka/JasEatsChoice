package com.xx.jaseatschoicejava.controller;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.LocationService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 定位控制器
 */
@RestController
@RequestMapping("/v1/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * 获取当前定位
     * @param latitude 纬度
     * @param longitude 经度
     * @param ip 客户端IP（可选）
     * @return 定位信息
     */
    @GetMapping
    public ResponseResult<?> getCurrentLocation(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String ip,
            HttpServletRequest request
    ) {
        if ((latitude == null) != (longitude == null)) {
            return ResponseResult.fail("400", "latitude与longitude需同时传递");
        }

        String effectiveIp = resolveClientIp(ip, request);
        if (latitude == null && longitude == null && !hasText(effectiveIp)) {
            return ResponseResult.fail("LOCATION_PARAM_MISSING", "未传递ip或经纬度信息无法定位");
        }

        Map<String, Object> location = locationService.getCurrentLocation(latitude, longitude, effectiveIp);
        return ResponseResult.success(location);
    }

    private String resolveClientIp(String requestIp, HttpServletRequest request) {
        if (isUsablePublicIp(requestIp)) {
            return requestIp.trim();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String headerIp = extractFirstUsableIp(xForwardedFor);
        if (hasText(headerIp)) {
            return headerIp;
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (isUsablePublicIp(xRealIp)) {
            return xRealIp.trim();
        }

        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (isUsablePublicIp(proxyClientIp)) {
            return proxyClientIp.trim();
        }

        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (isUsablePublicIp(wlProxyClientIp)) {
            return wlProxyClientIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        if (isUsablePublicIp(remoteAddr)) {
            return remoteAddr.trim();
        }

        return null;
    }

    private String extractFirstUsableIp(String xForwardedFor) {
        if (!hasText(xForwardedFor)) {
            return null;
        }

        String[] parts = xForwardedFor.split(",");
        for (String part : parts) {
            if (isUsablePublicIp(part)) {
                return part.trim();
            }
        }
        return null;
    }

    private boolean isUsablePublicIp(String ip) {
        if (!hasText(ip)) {
            return false;
        }

        String trimmed = ip.trim();
        if ("unknown".equalsIgnoreCase(trimmed)) {
            return false;
        }

        try {
            InetAddress address = InetAddress.getByName(trimmed);
            return !(address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress());
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 地址转坐标
     */
    @GetMapping("/geocode")
    public ResponseResult<?> geocode(
            @RequestParam String address,
            @RequestParam(required = false) String city
    ) {
        Map<String, Object> location = locationService.geocode(address, city);
        return ResponseResult.success(location);
    }

    /**
     * 坐标转地址
     */
    @GetMapping("/reverse-geocode")
    public ResponseResult<?> reverseGeocode(
            @RequestParam String lng,
            @RequestParam String lat
    ) {
        Map<String, Object> location = locationService.reverseGeocode(lng, lat);
        return ResponseResult.success(location);
    }

    /**
     * 获取级联选择器地址数据
     */
    @GetMapping("/cascader")
    public ResponseResult<?> getCascaderLocationData() {
        // 调用定位服务获取级联选择器地址数据
        List<Map<String, Object>> cascaderData = locationService.getCascaderLocationData();
        return ResponseResult.success(cascaderData);
    }

    /**
     * 获取客户端公网IP
     * 后端代理获取，避免前端CORS问题
     */
    @GetMapping("/public-ip")
    public ResponseResult<?> getPublicIp(HttpServletRequest request) {
        String clientIp = resolveClientIp(null, request);
        if (hasText(clientIp)) {
            return ResponseResult.success(Map.of("ip", clientIp));
        }
        return ResponseResult.fail("IP_NOT_FOUND", "无法获取公网IP");
    }

    /**
     * 地址搜索
     * @param address 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseResult<?> searchAddress(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String keywords
    ) {
        String query = address != null && !address.isEmpty() ? address : keywords;
        List<Map<String, Object>> searchResults = locationService.searchAddress(query);
        return ResponseResult.success(searchResults);
    }
}
