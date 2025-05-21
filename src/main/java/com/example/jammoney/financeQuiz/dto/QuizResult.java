package com.example.jammoney.financeQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {
    private boolean correct; //정답여부
    private String correctAnswer; //정답
    private String explanation; //해설
    private String hint; //힌트
}