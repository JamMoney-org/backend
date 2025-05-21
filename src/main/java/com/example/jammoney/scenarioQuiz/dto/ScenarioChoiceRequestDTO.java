package com.example.jammoney.scenarioQuiz.dto;

import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioChoiceRequestDTO {
    private Long scenarioId; // 어떤 시나리오인지 식별
    private String selectedChoice; // 사용자가 고른 문장
    private int currentStep;       // 현재 단계 (ex. 1단계 → 2단계)
}