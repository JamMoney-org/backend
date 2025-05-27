package com.example.jammoney.news.summary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class GemmaSummaryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemma.api-key}")
    private String apiKey;

    @Value("${gemma.base-url}")
    private String baseUrl;

    @Value("${gemma.endpoint}")
    private String endpoint;

    public String summarize(String text) {
        String url = baseUrl + endpoint + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", "Summarize the following financial news:\n\n" + text)
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "요약 실패: " + e.getMessage();
        }
    }
}
