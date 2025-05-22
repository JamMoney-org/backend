package com.example.jammoney.scenarioQuiz.gpt.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GptScenarioSummaryResponse {
    private String summary;  // 전체 플레이에 대한 평가
}