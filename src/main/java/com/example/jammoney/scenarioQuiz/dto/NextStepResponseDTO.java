package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextStepResponseDTO { //다음 Step 진행
    private int stepOrder; // 현재 진행 중인 스텝 번호 (예: 2단계)
    private String aiMessage; // GPT가 생성한 다음 질문
    private List<ScenarioChoiceDTO> choices;
    private boolean isFinalStep;
}