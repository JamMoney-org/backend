package com.example.jammoney.financeTerm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResultDto {
    private boolean isCorrect;
    private int correctAnswer;
    private int selectedAnswer; // 선택한 오답 표시용
}
