package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.AMapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 高德地图代理控制器
 * 通过后端代理高德地图 API，避免安全密钥暴露给前端
 */
@Api(tags = "高德地图代理")
@RestController
@RequestMapping("/v1/amap")
public class AMapController {

    private final AMapService aMapService;

    public AMapController(AMapService aMapService) {
        this.aMapService = aMapService;
    }

    /**
     * 地址关键词搜索
     */
    @ApiOperation("地址搜索")
    @GetMapping("/search")
    public ResponseResult<?> searchAddress(
            @ApiParam("搜索关键词") @RequestParam String keywords,
            @ApiParam("城市（可选，默认全国）") @RequestParam(required = false) String city
    ) {
        Map<String, Object> result = aMapService.searchAddress(keywords, city);
        return ResponseResult.success(result.get("data"), result.get("message").toString());
    }

    /**
     * 地理编码（地址 → 坐标）
     */
    @ApiOperation("地理编码")
    @GetMapping("/geocode")
    public ResponseResult<?> geocode(
            @ApiParam("地址") @RequestParam String address,
            @ApiParam("城市（可选）") @RequestParam(required = false) String city
    ) {
        Map<String, Object> result = aMapService.geocode(address, city);
        if ("200".equals(result.get("code"))) {
            return ResponseResult.success(result.get("data"), result.get("message").toString());
        } else {
            return ResponseResult.fail(result.get("code").toString(), result.get("message").toString());
        }
    }

    /**
     * 逆地理编码（坐标 → 地址）
     */
    @ApiOperation("逆地理编码")
    @GetMapping("/regeocode")
    public ResponseResult<?> regeocode(
            @ApiParam("经度") @RequestParam String lng,
            @ApiParam("纬度") @RequestParam String lat
    ) {
        Map<String, Object> result = aMapService.regeocode(lng, lat);
        if ("200".equals(result.get("code"))) {
            return ResponseResult.success(result.get("data"), result.get("message").toString());
        } else {
            return ResponseResult.fail(result.get("code").toString(), result.get("message").toString());
        }
    }

    /**
     * IP 定位（通过 IP 地址获取大概位置）
     */
    @ApiOperation("IP定位")
    @GetMapping("/ip/location")
    public ResponseResult<?> ipLocation() {
        Map<String, Object> result = aMapService.ipLocation();
        if ("200".equals(result.get("code"))) {
            return ResponseResult.success(result.get("data"), result.get("message").toString());
        } else {
            return ResponseResult.fail(result.get("code").toString(), result.get("message").toString());
        }
    }
}
