package com.example.Jammoney.StockApp.kis.service;

import com.example.Jammoney.StockApp.kis.entity.KisToken;
import com.example.Jammoney.StockApp.kis.repository.KisTokenRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class KisAuthService {

    @Value("${kis.app.key}")
    private String appKey;

    @Value("${kis.app.secret}")
    private String appSecret;

    @Value("${kis.base-url}")
    private String baseUrl;

    private final KisTokenRepository kisTokenRepository;

    public String getAccessToken() {
        KisToken token = kisTokenRepository.findById(1L).orElse(null);

        // 토큰이 없거나 만료되었을 때만 재발급
        if (token == null || token.getExpiredAt().isBefore(LocalDateTime.now())) {
            return requestNewToken(); // 저장하고 반환
        }

        return token.getToken();
    }

    private String requestNewToken() {
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

        String newToken = (String) result.get("access_token");
        int expiresIn = (int) result.get("expires_in");

        KisToken token = KisToken.builder()
                .id(1L)
                .token(newToken)
                .expiredAt(LocalDateTime.now().plusSeconds(expiresIn))
                .build();

        kisTokenRepository.save(token);
        return newToken;
    }
}
