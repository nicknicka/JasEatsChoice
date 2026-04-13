package com.xx.jaseatschoicejava.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.AMapService;
import com.xx.jaseatschoicejava.service.dto.AmapApiResponse;
import com.xx.jaseatschoicejava.service.dto.AmapPoiData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 旧高德地图代理控制器（兼容保留）
 *
 * 主业务入口已收敛到 LocationController / LocationServiceImpl。
 * 这里仅保留给历史调用方做兼容过渡。
 */
@Api(tags = "高德地图代理")
@Deprecated
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
        AmapApiResponse<java.util.List<AmapPoiData>> result = aMapService.searchAddress(keywords, city);
        if (result.isSuccess()) {
            return ResponseResult.success(result.data(), result.message());
        }
        return ResponseResult.fail(result.code(), result.message());
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
        AmapApiResponse<?> result = aMapService.geocode(address, city);
        if (result.isSuccess()) {
            return ResponseResult.success(result.data(), result.message());
        } else {
            return ResponseResult.fail(result.code(), result.message());
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
        AmapApiResponse<?> result = aMapService.regeocode(lng, lat);
        if (result.isSuccess()) {
            return ResponseResult.success(result.data(), result.message());
        } else {
            return ResponseResult.fail(result.code(), result.message());
        }
    }

    /**
     * IP 定位（通过 IP 地址获取大概位置）
     */
    @ApiOperation("IP定位")
    @GetMapping("/ip/location")
    public ResponseResult<?> ipLocation() {
        AmapApiResponse<?> result = aMapService.ipLocation();
        if (result.isSuccess()) {
            return ResponseResult.success(result.data(), result.message());
        } else {
            return ResponseResult.fail(result.code(), result.message());
        }
    }
}
