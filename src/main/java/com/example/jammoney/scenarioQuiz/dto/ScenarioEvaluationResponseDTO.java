package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioEvaluationResponseDTO { // 종료 후 평가 요청/응답
    private String overallFeedback; // GPT가 생성한 최종 피드백 문장
}