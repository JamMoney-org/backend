package com.example.jammoney.scenarioQuiz.dto;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.scenarioQuiz.entity.ScenarioCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioCreateRequestDTO {
    private String title;
    private String description;         // 상황 묘사
    private ScenarioCategory category;  // 카테고리
    private String firstAiMessage;      // 첫 질문
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;      // 난이도
}