package com.example.Jammoney.StockApp.kis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class KisAuthService {

    @Value("${kis.app.key}")
    private String appKey;

    @Value("${kis.app.secret}")
    private String appSecret;

    @Value("${kis.base-url}")
    private String baseUrl;

    private String accessToken;
    private LocalDateTime expiredAt;

    public String getAccessToken() {
        if (accessToken == null || expiredAt.isBefore(LocalDateTime.now())) {
            requestNewToken();
        }
        return accessToken;
    }

    private void requestNewToken() {
        String url = baseUrl + "/oauth2/tokenP";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        Map<String, Object> result = response.getBody();

        this.accessToken = (String) result.get("access_token");
        int expiresIn = (int) result.get("expires_in");
        this.expiredAt = LocalDateTime.now().plusSeconds(expiresIn);
    }
}

