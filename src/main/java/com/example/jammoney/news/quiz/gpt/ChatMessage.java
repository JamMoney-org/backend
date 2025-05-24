// src/main/java/com/example/jammoney/news/quiz/gpt/ChatMessage.java
package com.example.jammoney.news.quiz.gpt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenAI ChatCompletion 메시지 포맷용 간단 DTO
 */
public class ChatMessage {
    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

    // 생성자
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // getter / setter
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
