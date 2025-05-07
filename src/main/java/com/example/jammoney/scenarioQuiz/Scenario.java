package com.example.jammoney.scenarioQuiz;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {
    @Id
    @GeneratedValue
    private Long id;

    private String title;       // 예: "자취방 계약"
    private String description; // 첫 시작 설명
    private int rewardExp;      // 성공 시 경험치 보상
    private int rewardCoin;     // 성공 시 가상 머니 보상
}