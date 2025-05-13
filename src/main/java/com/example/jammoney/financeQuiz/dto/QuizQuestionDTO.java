package com.example.jammoney.financeQuiz.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionDTO {
    private String question;
    private List<String> options;   // 보기들
    private int correctIndex;       // 정답 위치
    private String hint;
    private String explanation;
}