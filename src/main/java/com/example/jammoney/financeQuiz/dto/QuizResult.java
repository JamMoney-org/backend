package com.example.jammoney.financeQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {
    private boolean correct;
    private String correctAnswer;
    private String explanation;
    private String hint;
}