package com.example.jammoney.financeQuiz.dto;

import lombok.Getter;

@Getter
public class QuizSubmitRequest {
    private FinanceQuiz quiz;       // 현재 푼 문제
    private int userAnswerIndex;    // 유저가 선택한 보기 index
}