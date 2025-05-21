package com.example.jammoney.scenarioQuiz.dto;

import java.util.List;

public class NextStepResponseDTO { //다음 Step 진행
    private int stepOrder;
    private String aiMessage;
    private List<ScenarioChoiceDTO> choices;
}