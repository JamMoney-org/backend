package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioStartResponseDTO { //시나리오 시작
    private Long scenarioId;            // 시나리오 ID
    private String title;               // 시나리오 제목
    private String description;         // 시나리오 설명
    private int stepOrder;              // 시작 단계 (항상 1)
    private String aiMessage;           // 첫 질문 메시지
    private List<ScenarioChoiceDTO> choices; // 사용자에게 보여줄 선택지 목록
}