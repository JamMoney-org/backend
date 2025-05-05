package com.example.jammoney.scenarioQuiz;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScenarioRecord {

    @Id
    @GeneratedValue
    private Long id;

    //@ManyToOne
    //private User user;

    @ManyToOne
    private ScenarioQuiz scenarioQuiz;  // 어떤 문제에 대한 기록인지

    private String userChoice;        // 유저가 선택한 보기

    private boolean correct;          // 정답 여부

    private LocalDateTime answeredAt;

    private int rewardedExp;

    private int rewardedMoney;
}