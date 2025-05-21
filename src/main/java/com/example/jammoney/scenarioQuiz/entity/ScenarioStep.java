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
public class ScenarioStep { //AI의 질문 (단계별 대화)
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Scenario scenario;

    private int stepOrder;           // 1, 2, 3...

    private String aiMessage;        // "어떤 조건이 가장 중요하신가요?"

    private boolean isEndStep;       // 이 단계가 마지막인지
}