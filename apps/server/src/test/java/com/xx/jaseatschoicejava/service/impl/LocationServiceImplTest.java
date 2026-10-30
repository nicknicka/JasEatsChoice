package com.xx.jaseatschoicejava.service.impl;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AMapService;
import com.xx.jaseatschoicejava.service.dto.AmapApiResponse;

@RunWith(MockitoJUnitRunner.class)
public class LocationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AMapService aMapService;

    private LocationServiceImpl locationService;

    private static final String GAODE_API_KEY = "test-api-key";
    private static final String GAODE_API_URL = "https://restapi.amap.com/v3";
    private static final String TENCENT_API_KEY = "test-tencent-key";
    private static final String TENCENT_API_URL = "https://apis.map.qq.com";

    @Before
    public void setUp() {
        locationService = new LocationServiceImpl(aMapService);
        ReflectionTestUtils.setField(locationService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(locationService, "gaodeApiKey", GAODE_API_KEY);
        ReflectionTestUtils.setField(locationService, "gaodeApiUrl", GAODE_API_URL);
        ReflectionTestUtils.setField(locationService, "tencentApiKey", TENCENT_API_KEY);
        ReflectionTestUtils.setField(locationService, "tencentApiUrl", TENCENT_API_URL);
    }

    @Test
    public void getCurrentLocation_ipLocation_shouldBuildTencentUrl() {
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
        Map<String, Object> result = locationService.getCurrentLocation(null, null, "1.2.3.4");

        String expectedUrl = String.format("%s/ws/location/v1/ip?key=%s&ip=%s", TENCENT_API_URL, TENCENT_API_KEY, "1.2.3.4");
        verify(restTemplate).getForEntity(eq(expectedUrl), eq(String.class));

        assertNotNull(result);
        assertEquals("广东省", result.get("province"));
        assertEquals("广州市", result.get("city"));
        assertEquals("113.27324", result.get("longitude").toString());
        assertEquals("23.15792", result.get("latitude").toString());
    }

    @Test
    public void getCurrentLocation_ipLocation_shouldParseTencentResult() {
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

        Map<String, Object> result = locationService.getCurrentLocation(null, null, null);

        assertNotNull(result);
        assertEquals("113.93041", result.get("longitude").toString());
        assertEquals("22.53332", result.get("latitude").toString());
    }

    @Test
    public void getCurrentLocation_ipLocation_shouldHandleEmptyResponse() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(""));

        BusinessException error = assertThrows(BusinessException.class,
                () -> locationService.getCurrentLocation(null, null, null));

        assertEquals("LOCATION_UPSTREAM_ERROR", error.getCode());
    }

    @Test
    public void getCurrentLocation_ipLocation_shouldHandleFailedStatus() {
        String mockResponse = """
            {
                "status": 1,
                "message": "INVALID_USER_KEY"
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        BusinessException error = assertThrows(BusinessException.class,
                () -> locationService.getCurrentLocation(null, null, null));

        assertEquals("LOCATION_UPSTREAM_ERROR", error.getCode());
    }

    @Test
    public void getCurrentLocation_ipLocation_shouldHandleException() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("网络错误"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> locationService.getCurrentLocation(null, null, null));

        assertEquals("LOCATION_UPSTREAM_ERROR", error.getCode());
    }

    @Test
    public void getCurrentLocation_tencentQuotaExhausted_shouldFallbackToGaodeIp() {
        String tencentQuotaResponse = """
            {
                "status": 121,
                "message": "此key每日调用量已达到上限"
            }
            """;
        String gaodeIpResponse = """
            {
                "status": "1",
                "info": "OK",
                "infocode": "10000",
                "province": "北京市",
                "city": "北京市",
                "adcode": "110000",
                "rectangle": "116.0119343,39.66127144;116.7829835,40.2164962"
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            return ResponseEntity.ok(url.contains("apis.map.qq.com") ? tencentQuotaResponse : gaodeIpResponse);
        });

        Map<String, Object> result = locationService.getCurrentLocation(null, null, "220.181.38.148");

        assertNotNull(result);
        assertEquals("北京市", result.get("province"));
        assertEquals("北京市", result.get("city"));
        assertEquals("116.0119343", result.get("longitude").toString());
        assertEquals("39.66127144", result.get("latitude").toString());
        assertEquals("city", result.get("accuracy"));
        verify(restTemplate).getForEntity(
                argThat((String url) -> url.contains("restapi.amap.com/v3/ip")
                        && url.contains("ip=220.181.38.148")),
                eq(String.class));
    }

    @Test
    public void getCurrentLocation_bothProvidersUnavailable_shouldThrow() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"status\":121,\"message\":\"此key每日调用量已达到上限\"}"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> locationService.getCurrentLocation(null, null, "220.181.38.148"));

        assertEquals("LOCATION_UPSTREAM_ERROR", error.getCode());
    }

    @Test
    public void getCurrentLocation_gaodeIpReturnsLanPlaceholder_shouldThrow() {
        String tencentQuotaResponse = """
            {
                "status": 121,
                "message": "此key每日调用量已达到上限"
            }
            """;
        String gaodeLanResponse = """
            {
                "status": "1",
                "info": "OK",
                "province": "局域网",
                "city": [],
                "adcode": [],
                "rectangle": []
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            return ResponseEntity.ok(url.contains("apis.map.qq.com") ? tencentQuotaResponse : gaodeLanResponse);
        });

        BusinessException error = assertThrows(BusinessException.class,
                () -> locationService.getCurrentLocation(null, null, "192.168.1.5"));

        assertEquals("LOCATION_UPSTREAM_ERROR", error.getCode());
    }

    @Test
    public void getCurrentLocation_withoutAmapKey_shouldFailExplicitly() {
        ReflectionTestUtils.setField(locationService, "gaodeApiKey", "");

        BusinessException error = assertThrows(BusinessException.class,
                () -> locationService.getCurrentLocation(39.90923, 116.397428, null));

        assertEquals("LOCATION_KEY_MISSING", error.getCode());
    }

    @Test
    public void getCurrentLocation_withoutTencentKey_shouldFailExplicitly() {
        ReflectionTestUtils.setField(locationService, "tencentApiKey", "");

        BusinessException error = assertThrows(BusinessException.class,
                () -> locationService.getCurrentLocation(null, null, "1.2.3.4"));

        assertEquals("LOCATION_KEY_MISSING", error.getCode());
    }

    @Test
    public void getCurrentLocation_reverseGeocode_shouldBuildCorrectUrl() {
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
