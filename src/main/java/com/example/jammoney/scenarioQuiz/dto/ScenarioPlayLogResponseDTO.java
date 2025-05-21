package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScenarioPlayLogResponseDTO {
    private Long id;
    private Long userId;
    private Long scenarioId;
    private int stepOrder;
    private LocalDateTime selectedAt;
    private String choiceContent;
    private String feedback;
}