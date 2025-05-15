package com.example.jammoney.financeQuiz.dto;

import com.example.jammoney.financeQuiz.entity.QuizCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WrongNoteResponse {
    private Long id;
    private String question;
    private String selectedOption;
    private String correctAnswer;
    private String explanation;
    private String hint;
    private QuizCategory category;
}