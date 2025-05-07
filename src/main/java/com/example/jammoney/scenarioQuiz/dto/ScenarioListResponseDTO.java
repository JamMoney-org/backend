package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioListResponseDTO { //시나리오 목록 조회용
    private Long scenarioId;
    private String title;
    private String description;
}