package com.example.jammoney.scenarioQuiz.dto;

public class ScenarioChoiceRequestDTO { //선택지 응답
    private Long choiceId;
    private String content;
    private boolean isEnd;         // 이 선택이 마지막인지 (선택 후 안내용)
}