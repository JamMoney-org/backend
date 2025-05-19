package com.example.jammoney.financeQuiz.gpt.dto;

import lombok.Data;

import java.util.List;

@Data
public class GptResponse {
    private List<Choice> choices;

    public String getContent() {
        return choices.get(0).getMessage().getContent();
    }

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}