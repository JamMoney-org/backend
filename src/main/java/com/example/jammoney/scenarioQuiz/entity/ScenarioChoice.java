package com.example.jammoney.scenarioQuiz.entity;

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
public class ScenarioChoice { //사용자에게 보여줄 선택지
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private ScenarioStep step;

    private String content;         // 예: “햇빛 잘 들어오나요?”
    private boolean isGoodChoice;   // 긍정적 선택인지
    private boolean isEnd;          // 이 선택으로 종료되는지
    private String feedback;        // 선택 시 보여줄 피드백
    private Integer nextStepOrder;  // 다음 단계 (null이면 종료)
}