package com.xx.jaseatschoicejava.service.impl;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.xx.jaseatschoicejava.exception.BusinessException;

@RunWith(MockitoJUnitRunner.class)
public class WeatherServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private WeatherServiceImpl weatherService;

    private static final String WEATHER_URL = "https://restapi.amap.com/v3/weather/weatherInfo";
    private static final String WEATHER_KEY = "test-weather-key";

    @Before
    public void setUp() {
        weatherService = new WeatherServiceImpl();
        ReflectionTestUtils.setField(weatherService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(weatherService, "gaodeApiKey", WEATHER_KEY);
        ReflectionTestUtils.setField(weatherService, "gaodeApiWeatherUrl", WEATHER_URL);
    }

    @Test
    public void getWeatherInfo_shouldReturnRealWeatherFields() {
        String mockResponse = """
            {
                "status": "1",
                "count": "1",
                "lives": [{
                    "province": "北京",
                    "city": "北京市",
                    "weather": "晴",
                    "temperature": "12",
                    "winddirection": "西北",
                    "windpower": "3",
                    "humidity": "40",
                    "reporttime": "2026-10-28 10:00:00"
                }]
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        Map<String, Object> result = weatherService.getWeatherInfo("北京");

        verify(restTemplate).getForEntity(
                eq(WEATHER_URL + "?key=" + WEATHER_KEY + "&city=北京&extensions=base"),
                eq(String.class));
        assertEquals("北京市", result.get("city"));
        assertEquals("12", result.get("temperature"));
        assertEquals("40", result.get("humidity"));
        assertEquals("晴", result.get("condition"));
        assertEquals("3", result.get("windSpeed"));
        assertEquals("西北", result.get("windDirection"));
        assertEquals("2026-10-28 10:00:00", result.get("reportTime"));
    }

    @Test
    public void getWeatherInfo_withoutKey_shouldFailExplicitly() {
        ReflectionTestUtils.setField(weatherService, "gaodeApiKey", "");

        BusinessException error = assertThrows(BusinessException.class,
                () -> weatherService.getWeatherInfo("北京"));

        assertEquals("WEATHER_KEY_MISSING", error.getCode());
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void getWeatherInfo_whenUpstreamFails_shouldNotReturnFakeData() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"status\":\"0\",\"info\":\"INVALID_USER_KEY\"}"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> weatherService.getWeatherInfo("北京"));

        assertEquals("WEATHER_UPSTREAM_ERROR", error.getCode());
    }
}
