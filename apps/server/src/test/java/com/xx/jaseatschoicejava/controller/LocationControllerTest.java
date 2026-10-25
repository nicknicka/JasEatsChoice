package com.xx.jaseatschoicejava.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.xx.jaseatschoicejava.service.LocationService;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        LocationController controller = new LocationController(locationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCurrentLocation_shouldReturnSuccess() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("province", "北京市");
        data.put("city", "北京市");
        data.put("district", "东城区");
        data.put("longitude", "116.397428");
        data.put("latitude", "39.90923");

        when(locationService.getCurrentLocation(null, null, "1.2.3.4")).thenReturn(data);

        mockMvc.perform(get("/v1/location")
                        .header("X-Forwarded-For", "1.2.3.4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.city").value("北京市"))
                .andExpect(jsonPath("$.data.longitude").value("116.397428"));

        verify(locationService).getCurrentLocation(null, null, "1.2.3.4");
    }

    @Test
    void getCurrentLocation_withLatLng_shouldPassParamsToService() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("address", "北京市东城区");
        data.put("longitude", "116.397428");
        data.put("latitude", "39.90923");

        when(locationService.getCurrentLocation(eq(39.90923), eq(116.397428), ArgumentMatchers.isNull())).thenReturn(data);

        mockMvc.perform(get("/v1/location")
                        .param("latitude", "39.90923")
                        .param("longitude", "116.397428"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.address").value("北京市东城区"));

        verify(locationService).getCurrentLocation(eq(39.90923), eq(116.397428), ArgumentMatchers.isNull());
        }

        @Test
        void getCurrentLocation_withIpParam_shouldPassIpToService() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("city", "北京市");
        data.put("longitude", "116.397428");
        data.put("latitude", "39.90923");

        when(locationService.getCurrentLocation(null, null, "8.8.8.8")).thenReturn(data);

        mockMvc.perform(get("/v1/location")
                .param("ip", "8.8.8.8"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.city").value("北京市"));

        verify(locationService).getCurrentLocation(null, null, "8.8.8.8");
        }

        @Test
        void getCurrentLocation_withoutLatLngAndIp_shouldReturnFail() throws Exception {
        mockMvc.perform(get("/v1/location"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("LOCATION_PARAM_MISSING"))
            .andExpect(jsonPath("$.message").value("未传递ip或经纬度信息无法定位"));
    }

    @Test
    void reverseGeocode_shouldReturnSuccess() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("formattedAddress", "北京市东城区东华门街道");
        data.put("province", "北京市");

        when(locationService.reverseGeocode("116.397428", "39.90923")).thenReturn(data);

        mockMvc.perform(get("/v1/location/reverse-geocode")
                        .param("lng", "116.397428")
                        .param("lat", "39.90923"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.formattedAddress").value("北京市东城区东华门街道"));

        verify(locationService).reverseGeocode("116.397428", "39.90923");
    }

    @Test
    void getCascaderLocationData_shouldReturnSuccess() throws Exception {
        Map<String, Object> item = new HashMap<>();
        item.put("label", "北京市");
        item.put("value", "110000");
        when(locationService.getCascaderLocationData()).thenReturn(List.of(item));

        mockMvc.perform(get("/v1/location/cascader"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].label").value("北京市"));

        verify(locationService).getCascaderLocationData();
    }
}