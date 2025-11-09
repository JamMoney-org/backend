package com.example.jammoney.scenarioQuiz.entity;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario { //시나리오 기본 정보
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ScenarioCategory category;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
}