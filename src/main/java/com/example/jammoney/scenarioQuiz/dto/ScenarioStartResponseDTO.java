package com.example.jammoney.scenarioQuiz.dto;

import java.util.List;

public class ScenarioStartResponseDTO { //시나리오 시작
    private Long scenarioId;
    private String title;
    private String description;

    private int stepOrder;           // 1
    private String aiMessage;        // 첫 질문
    private List<ScenarioChoiceDTO> choices;
}