package com.example.jammoney.scenarioQuiz.entity;

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

    private String title;       // 예: "자취방 계약"
    private String description; // 첫 시작 설명

    @Enumerated(EnumType.STRING)
    private ScenarioCategory category;
}