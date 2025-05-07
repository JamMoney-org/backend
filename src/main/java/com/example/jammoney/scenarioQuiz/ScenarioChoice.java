package com.example.jammoney.scenarioQuiz;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioChoice {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private ScenarioStep step;

    private String content;          // 사용자 선택지 텍스트

    private boolean isGoodChoice;    // 긍정적 선택인지
    private boolean isEnd;           // 이 선택으로 종료되는지
    private String feedback;         // 선택 시 AI 피드백
    private Integer nextStepOrder;   // 다음으로 이동할 stepOrder (null이면 종료)
}