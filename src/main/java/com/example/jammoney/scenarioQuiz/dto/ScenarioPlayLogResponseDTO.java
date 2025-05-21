package com.example.jammoney.scenarioQuiz.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScenarioPlayLogResponseDTO {
    private Long id; // 로그 ID
    private Long userId; // 해당 로그를 남긴 사용자 ID
    private Long scenarioId; // 관련된 시나리오 ID
    private int stepOrder; // 몇 번째 단계인지
    private LocalDateTime selectedAt; // 선택 시각
    private String choiceContent; // 사용자가 선택한 내용
}