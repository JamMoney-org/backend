package com.example.jammoney.scenarioQuiz;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioQuiz {
    @Id
    @GeneratedValue
    private Long id;

    private String situation;         // 문제 설명

    @ElementCollection
    private List<String> choices;     // 선택지 목록

    private String correctAnswer;     // 정답

    private String explanation;       // 해설

    @ElementCollection
    private List<String> relatedConcepts;  // ["저축", "소비"]

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;    // 🔥 난이도: EASY / MEDIUM / HARD
}
