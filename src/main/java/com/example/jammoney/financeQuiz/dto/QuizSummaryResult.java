package com.example.jammoney.financeQuiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizSummaryResult {
    private int totalQuestions;
    private int correctCount;
    private int rewardExp;
    private int rewardCoin;
    private boolean passed; // 3개 이상 정답이면 true
}