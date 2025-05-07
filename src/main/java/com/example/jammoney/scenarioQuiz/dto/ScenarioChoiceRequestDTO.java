package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioChoiceRequestDTO { //선택지 제출 요청
    private Long scenarioId;
    private int currentStepOrder;
    private Long selectedChoiceId;
}