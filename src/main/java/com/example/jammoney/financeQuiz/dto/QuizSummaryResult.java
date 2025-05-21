package com.example.jammoney.financeQuiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizSummaryResult {
    private int totalQuestions; //전체퀴즈개수
    private int correctCount; //맞은퀴즈개수
    private int rewardExp; //경험치
    private int rewardCoin; //가상머니
    private boolean passed; // 3개 이상 정답이면 true
}