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
public class ScenarioStep {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Scenario scenario;

    private int stepOrder;           // 1, 2, 3...
    private String aiMessage;        // AI가 사용자에게 말하는 질문
}