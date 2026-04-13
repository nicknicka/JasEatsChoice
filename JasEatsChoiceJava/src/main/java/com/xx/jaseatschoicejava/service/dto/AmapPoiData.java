package com.xx.jaseatschoicejava.service.dto;

/**
 * 高德 POI 搜索结果项。
 */
public record AmapPoiData(
    String name,
    String address,
    String pname,
    AmapPoiLocation location
) {
}