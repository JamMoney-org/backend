package com.example.jammoney.scenarioQuiz.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptScenarioChoiceResponse {
    private List<GptChoiceData> choices;
}