package com.xx.jaseatschoicejava.service.dto;

/**
 * 高德位置数据，兼容地理编码、逆地理编码和 IP 定位返回。
 */
public record AmapLocationData(
    Double lng,
    Double lat,
    String province,
    String city,
    String accuracy,
    String formattedAddress
) {
}