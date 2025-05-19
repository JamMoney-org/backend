package com.example.jammoney.financeQuiz.gpt.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1") // OpenAI API 기본 URL
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}