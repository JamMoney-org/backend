package com.example.jammoney.financeQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerResultResponseDTO {
    private boolean isCorrect;
    private int correctIndex;
    private String explanation;
    private String hint;
}