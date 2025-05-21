package com.example.jammoney.financeTerm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResultDto { //퀴즈 제출 결과를 반환할 때 사용 (맞췄는지, 정답은 무엇인지)
    private boolean isCorrect;
    private int correctAnswer;
    private int selectedAnswer; // 선택한 오답 표시용
}
