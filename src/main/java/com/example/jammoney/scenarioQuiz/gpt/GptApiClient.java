package com.example.jammoney.scenarioQuiz.gpt;

import com.example.jammoney.scenarioQuiz.gpt.dto.GptChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GptApiClient {

    private final WebClient webClient;

    @Value("${gpt.api.key}")
    private String gptApiKey;

    public Mono<GptChatResponse> callGpt(String prompt) {
        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gptApiKey)
                .bodyValue(buildRequest(prompt))
                .retrieve()
                .bodyToMono(GptChatResponse.class);
    }

    private Map<String, Object> buildRequest(String prompt) {
        return Map.of(
                "model", "gpt-3.5-turbo",
                "temperature", 0.7,
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 금융 교육을 돕는 ai야. 시나리오 퀴즈를 안내해야 해."),
                        Map.of("role", "user", "content", prompt)
                )
        );
    }
}