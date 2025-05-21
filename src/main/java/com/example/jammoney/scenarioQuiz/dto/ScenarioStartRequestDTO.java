package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioStartRequestDTO { //시나리오 시작
    private Long scenarioId;       // 선택한 시나리오 ID
}