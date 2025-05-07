package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioChoiceDTO { //선택지 응답
    private Long choiceId;
    private String content;
}