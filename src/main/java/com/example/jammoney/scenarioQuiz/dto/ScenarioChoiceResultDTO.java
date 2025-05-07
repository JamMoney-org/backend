package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioChoiceResultDTO { //선택지 결과 응답
    private boolean isCorrect;
    private String feedback;
    private boolean isEnd;
    private ScenarioStepResponseDTO nextStep; // isEnd가 false일 경우에만 포함
}