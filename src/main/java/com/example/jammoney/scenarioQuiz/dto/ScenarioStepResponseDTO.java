package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioStepResponseDTO { //시나리오 진행 응답
    private int stepOrder;
    private String aiMessage;
    private List<ScenarioChoiceDTO> choices;
}