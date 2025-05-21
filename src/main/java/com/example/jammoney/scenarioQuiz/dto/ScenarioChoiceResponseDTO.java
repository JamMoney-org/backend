package com.example.jammoney.scenarioQuiz.dto;

public class ScenarioChoiceResponseDTO { //선택지 응답
    private String feedback;         // GPT가 반환한 피드백
    private boolean isEnd;           // 이 선택으로 시나리오가 종료되는지
}