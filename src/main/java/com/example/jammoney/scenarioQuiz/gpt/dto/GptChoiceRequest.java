package com.example.jammoney.scenarioQuiz.gpt.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptChoiceRequest {
    private String currentAiMessage;   // 현재 AI 메시지
    private List<String> history;      // 이전 선택 이력 (선택된 content들)
}