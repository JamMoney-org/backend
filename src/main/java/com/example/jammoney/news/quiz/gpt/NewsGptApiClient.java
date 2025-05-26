package com.example.jammoney.news.quiz.gpt;

import com.example.jammoney.scenarioQuiz.gpt.dto.GptChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.jammoney.news.quiz.gpt.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component("newsGptApiClient")
@RequiredArgsConstructor
public class NewsGptApiClient {
    private final WebClient webClient;

    @Value("${gpt.api.key}")
    private String gptApiKey;

    public Mono<GptChatResponse> callGpt(String prompt) {
        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gptApiKey)
                .bodyValue(Map.of(
                        "model", "gpt-3.5-turbo",
                        "temperature", 0.7,
                        "messages", List.of(
                                Map.of("role","system","content","너는 금융 퀴즈 생성 AI야."),
                                Map.of("role","user","content", prompt)
                        )
                ))
                .retrieve()
                .bodyToMono(GptChatResponse.class);
    }

    public Mono<String> callGptRaw(String prompt) {
        return callGpt(prompt)
                .map(r -> r.getChoices().get(0).getMessage().getContent());
    }

    public Mono<String> callChatCompletionRaw(List<ChatMessage> messages) {
        return webClient.post()
                .uri("/chat/completions")
                .headers(headers -> headers.setBearerAuth(gptApiKey))
                .bodyValue(Map.of(
                        "model", "gpt-3.5-turbo",
                        "temperature", 0.7,
                        "messages", messages
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> node
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText()
                );
    }
}
