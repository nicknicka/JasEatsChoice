package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 通过 HTTP 只读访问 dsh 本地 Management API（GET state / GET tickets）。
 * 面板只监听 localhost，因此 base-url 默认指向回环地址。
 */
@Component
public class DshManagementClientHttpImpl implements DshManagementClient {

    @Value("${agent.dsh.base-url:http://127.0.0.1:8099}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public DshManagementClientHttpImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchState() {
        try {
            return restTemplate.getForObject(baseUrl + "/state", Map.class);
        } catch (Exception e) {
            throw new BusinessException("503", "dsh Management API 不可用：" + e.getClass().getSimpleName());
        }
    }

    @Override
    public List<Map<String, Object>> fetchTickets() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl + "/tickets", HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {
                    });
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (Exception e) {
            throw new BusinessException("503", "dsh Management API 不可用：" + e.getClass().getSimpleName());
        }
    }
}
