package com.example.jammoney.scenarioQuiz.dto;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioListResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
}