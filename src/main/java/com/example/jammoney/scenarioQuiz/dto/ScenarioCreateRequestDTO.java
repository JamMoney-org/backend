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
    private String title;               // 예: 자취방 계약
    private String description;         // 예: 대학생이 자취를 시작하는 상황
    private ScenarioCategory category;  // 예: 소비
    private String firstAiMessage;      // 예: "어떤 조건의 자취방을 찾으시나요?"
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
}