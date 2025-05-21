package com.example.jammoney.scenarioQuiz.gpt.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptSummaryRequest {
    private List<String> selectedChoices;  // 전체 선택 흐름만 필요
}