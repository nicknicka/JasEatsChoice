package com.xx.jaseatschoicejava.service.bridge;

import com.xx.jaseatschoicejava.config.GitHubBridgeProperties;
import com.xx.jaseatschoicejava.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHub REST 适配器。未配置 token/repo 时明确拒绝，而不是静默伪造 Issue。
 */
@Component
public class HttpGitHubIssueClient implements GitHubIssueClient {

    @Autowired
    private GitHubBridgeProperties properties;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public CreatedIssue createIssue(String repo, String title, String body, List<String> labels) {
        requireConfigured();
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("body", body);
        payload.put("labels", labels);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                properties.getApiBase() + "/repos/" + repo + "/issues",
                new HttpEntity<>(payload, headers()), Map.class);
        Map<String, Object> result = response.getBody();
        if (result == null || result.get("number") == null) {
            throw new BusinessException("502", "GitHub 返回内容不完整");
        }
        return new CreatedIssue(String.valueOf(result.get("number")), String.valueOf(result.get("html_url")));
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getState(String repo, String issueNumber) {
        requireConfigured();
        ResponseEntity<Map> response = restTemplate.exchange(
                properties.getApiBase() + "/repos/" + repo + "/issues/" + issueNumber,
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers()), Map.class);
        Map<String, Object> result = response.getBody();
        return result == null ? "open" : String.valueOf(result.get("state"));
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(properties.getToken());
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }

    private void requireConfigured() {
        if (!properties.isConfigured()) {
            throw new BusinessException("503", "GitHub 桥接未配置");
        }
    }
}
