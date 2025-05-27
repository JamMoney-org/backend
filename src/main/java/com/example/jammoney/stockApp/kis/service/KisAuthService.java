package com.example.jammoney.stockApp.kis.service;

import com.example.jammoney.stockApp.kis.entity.KisToken;
import com.example.jammoney.stockApp.kis.repository.KisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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

    private static final int TOKEN_EXPIRE_BUFFER_SEC = 30;

    public synchronized String getAccessToken() {
        KisToken token = kisTokenRepository.findById(1L).orElse(null);
        if (token != null && token.getExpiredAt().isAfter(LocalDateTime.now().plusSeconds(TOKEN_EXPIRE_BUFFER_SEC))) {
            log.info("유효한 토큰 반환 (DB 저장된 만료 시각): {}", token.getExpiredAt());
            return token.getToken();
        }
        log.info("토큰 만료 또는 없음 → 새 토큰 발급 요청");
        return requestNewToken();
    }

    String requestNewToken() {
        String url = baseUrl + "/oauth2/tokenP";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> result = response.getBody();

            String newToken = (String) result.get("access_token");
            String expiredAtString = (String) result.get("access_token_token_expired");

            LocalDateTime expiredAt = LocalDateTime.parse(expiredAtString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            KisToken token = KisToken.builder()
                    .id(1L)
                    .token(newToken)
                    .expiredAt(expiredAt)
                    .build();

            kisTokenRepository.save(token);
            return newToken;

        } catch (HttpClientErrorException.Forbidden e) {
            log.warn("토큰 발급 제한 (403) 발생: 기존 토큰 재사용 시도");

            // 기존 토큰이 있는지 안전하게 확인 후 반환
            return kisTokenRepository.findById(1L)
                    .map(KisToken::getToken)
                    .orElseThrow(() -> new IllegalStateException("토큰 발급 실패 + 기존 토큰 없음"));
        }
    }
}
