package com.xx.jaseatschoicejava.service.impl;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.xx.jaseatschoicejava.service.AMapService;
import com.xx.jaseatschoicejava.service.dto.AmapApiResponse;
import com.xx.jaseatschoicejava.service.dto.AmapLocationData;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AMapService aMapService;

    private LocationServiceImpl locationService;

    private static final String GAODE_API_KEY = "test-api-key";
    private static final String GAODE_API_URL = "https://restapi.amap.com/v3";
    private static final String TENCENT_API_KEY = "YT2BZ-EC5LJ-VUEF7-X76XQ-6HELK-JWFNQ";
    private static final String TENCENT_API_URL = "https://apis.map.qq.com";

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl(aMapService);
        ReflectionTestUtils.setField(locationService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(locationService, "gaodeApiKey", GAODE_API_KEY);
        ReflectionTestUtils.setField(locationService, "gaodeApiUrl", GAODE_API_URL);
        ReflectionTestUtils.setField(locationService, "tencentApiKey", TENCENT_API_KEY);
        ReflectionTestUtils.setField(locationService, "tencentApiUrl", TENCENT_API_URL);
    }

    @Test
    void getCurrentLocation_ipLocation_shouldBuildTencentUrl() {
        String mockResponse = """
            {
                "status": 0,
                "message": "Success",
                "request_id": "3efaa2a4a3994f7d8005955844834fb7",
                "result": {
                    "ip": "120.230.83.183",
                    "location": {
                        "lat": 23.15792,
                        "lng": 113.27324
                    },
                    "ad_info": {
                        "nation": "中国",
                        "province": "广东省",
                        "city": "广州市",
                        "district": "白云区",
                        "adcode": 440111,
                        "nation_code": 156
                    }
                }
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        when(aMapService.geocode(eq("白云区"), eq("广州市")))
                .thenReturn(new AmapApiResponse<>("200", "搜索成功",
                    new AmapLocationData(113.27324, 23.15792, "广东省", "广州市", "city", "广东省广州市白云区")));

        Map<String, Object> result = locationService.getCurrentLocation(null, null, "1.2.3.4");

        String expectedUrl = String.format("%s/ws/location/v1/ip?key=%s&ip=%s", TENCENT_API_URL, TENCENT_API_KEY, "1.2.3.4");
        verify(restTemplate).getForEntity(eq(expectedUrl), eq(String.class));
        verify(aMapService).geocode(eq("白云区"), eq("广州市"));

        assertNotNull(result);
        assertEquals("广东省", result.get("province"));
        assertEquals("广州市", result.get("city"));
        assertEquals("113.27324", result.get("longitude").toString());
        assertEquals("23.15792", result.get("latitude").toString());
    }

    @Test
    void getCurrentLocation_ipLocation_shouldParseTencentResult() {
        String mockResponse = """
            {
                "status": 0,
                "message": "Success",
                "result": {
                    "location": {
                        "lat": 22.53332,
                        "lng": 113.93041
                    },
                    "ad_info": {
                        "nation": "中国",
                        "province": "广东省",
                        "city": "深圳市",
                        "district": "南山区"
                    }
                }
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        when(aMapService.geocode(eq("南山区"), eq("深圳市")))
                .thenReturn(new AmapApiResponse<>("200", "搜索成功",
                    new AmapLocationData(113.93041, 22.53332, "广东省", "深圳市", "city", "广东省深圳市南山区")));

        Map<String, Object> result = locationService.getCurrentLocation(null, null, null);

        assertNotNull(result);
        assertEquals("113.93041", result.get("longitude").toString());
        assertEquals("22.53332", result.get("latitude").toString());
    }

    @Test
    void getCurrentLocation_ipLocation_shouldHandleEmptyResponse() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(""));

        Map<String, Object> result = locationService.getCurrentLocation(null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentLocation_ipLocation_shouldHandleFailedStatus() {
        String mockResponse = """
            {
                "status": 1,
                "message": "INVALID_USER_KEY"
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        Map<String, Object> result = locationService.getCurrentLocation(null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentLocation_ipLocation_shouldHandleException() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("网络错误"));

        Map<String, Object> result = locationService.getCurrentLocation(null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentLocation_reverseGeocode_shouldBuildCorrectUrl() {
        String mockResponse = """
            {
                "status": "1",
                "info": "OK",
                "infocode": "10000",
                "regeocode": {
                    "formatted_address": "北京市东城区东华门街道",
                    "addressComponent": {
                        "province": "北京市",
                        "city": "北京市",
                        "district": "东城区"
                    }
                }
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        Double latitude = 39.90923;
        Double longitude = 116.397428;

        Map<String, Object> result = locationService.getCurrentLocation(latitude, longitude, null);

        verify(restTemplate).getForEntity(contains("/geocode/regeo"), eq(String.class));

        assertNotNull(result);
        assertEquals("北京市东城区东华门街道", result.get("address"));
        assertEquals("北京市", result.get("province"));
        assertEquals("北京市", result.get("city"));
        assertEquals("东城区", result.get("district"));
    }
}
