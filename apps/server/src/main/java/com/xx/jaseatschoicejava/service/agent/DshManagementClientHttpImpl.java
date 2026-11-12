package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 通过 HTTP 只读访问 dsh 本地 Management API（GET state / GET sources）。
 *
 * <p>面板只监听回环地址，因此 base-url 默认指向 127.0.0.1。后端跑在容器里时要改成
 * host.docker.internal（并注意 prototype 会校验 Host 头，可能需 host 网络或 Host 改写，
 * 端到端待运行时就绪后再确认，见 issue #553）。
 */
@Component
public class DshManagementClientHttpImpl implements DshManagementClient {

    /** dsh-superagent-prototype 的 Management API 前缀。 */
    private static final String API_PREFIX = "/orchestrator/api";
    /** 必须携带，否则 403。 */
    private static final String REQUEST_HEADER = "x-orchestrator-request";

    @Value("${agent.dsh.base-url:http://127.0.0.1:8099}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public DshManagementClientHttpImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, Object> fetchState() {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + API_PREFIX + "/state", HttpMethod.GET, entity(),
                    new ParameterizedTypeReference<>() {
                    });
            return response.getBody() == null ? Map.of() : response.getBody();
        } catch (Exception e) {
            throw new BusinessException("503", "dsh Management API 不可用：" + e.getClass().getSimpleName());
        }
    }

    @Override
    public List<Map<String, Object>> fetchSources() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl + API_PREFIX + "/sources", HttpMethod.GET, entity(),
                    new ParameterizedTypeReference<>() {
                    });
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (Exception e) {
            throw new BusinessException("503", "dsh Management API 不可用：" + e.getClass().getSimpleName());
        }
    }

    private HttpEntity<Void> entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        // prototype 只监听回环并要求该头，缺失即 403。
        headers.set(REQUEST_HEADER, "1");
        return new HttpEntity<>(headers);
    }
}
