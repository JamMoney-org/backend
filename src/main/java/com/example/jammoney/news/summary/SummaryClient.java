package com.example.jammoney.news.summary;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class SummaryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gpt.api.key}")
    private String apiKey;

    @Value("${gpt.base-url}")
    private String endpoint;

    @Value("${gpt.model}")
    private String model;

    public String summarize(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> payload = Map.of(
                "model", model,
                "temperature", 0.7,
                "messages", List.of(
                        Map.of("role","system",
                                "content","You are a helpful assistant that summarizes text into 3 concise bullet points."),
                        Map.of("role","user",
                                "content","Summarize the following in 3 bullet points:\n\n" + text)
                )
        );

        HttpEntity<Map<String,Object>> req = new HttpEntity<>(payload, headers);
        ResponseEntity<ChatResponse> resp =
                restTemplate.postForEntity(endpoint, req, ChatResponse.class);

        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new RuntimeException("GPT 요약 실패: " + resp.getStatusCode());
        }

        return resp.getBody()
                .choices.get(0)
                .message
                .content
                .trim();
    }

    public static class ChatResponse {
        public List<Choice> choices;
        public static class Choice {
            public Message message;
        }
        public static class Message {
            public String role;
            public String content;
        }
    }
}
