package com.example.jammoney.scenarioQuiz.dto;

import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextStepResponseDTO { //다음 Step 진행
    private int stepOrder;
    private String aiMessage;
    private List<ScenarioChoiceDTO> choices;
    private boolean isFinalStep;
}