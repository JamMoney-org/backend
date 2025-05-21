package com.example.jammoney.scenarioQuiz.dto;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioChoiceDTO {
    private Long choiceId;
    private String content;
    private boolean isGood;
    private boolean isEnd;
}