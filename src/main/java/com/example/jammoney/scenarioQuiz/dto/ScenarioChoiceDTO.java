package com.example.jammoney.scenarioQuiz.dto;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioChoiceDTO {
    private Long choiceId; // 선택지 ID
    private String content; // 실제 선택지 내용 (예: "보증금 500만원 이하")
    private boolean isGood; // 올바른 선택인지 여부
    private boolean isEnd; // 이 선택지를 고르면 시나리오 종료되는지 여부
}