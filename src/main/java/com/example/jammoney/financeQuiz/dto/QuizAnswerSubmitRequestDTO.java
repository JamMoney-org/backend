package com.example.jammoney.financeQuiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerSubmitRequestDTO {
    private int selectedIndex;   // 유저가 고른 보기 index
    private QuizQuestionDTO questionData;  // 해당 문제 전체
}