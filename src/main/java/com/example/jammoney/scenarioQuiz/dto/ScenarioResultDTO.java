package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioResultDTO { //시나리오 완료 시 최종 응답
    private Long scenarioId;
    private String title;
    private int earnedExp;
    private int earnedCoin;
    private String resultMessage;
}