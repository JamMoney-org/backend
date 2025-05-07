package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioStartResponseDTO { //시나리오 시작 응답
    private Long scenarioId;
    private int stepOrder;
    private String aiMessage;
    private List<ScenarioChoiceDTO> choices;
}
