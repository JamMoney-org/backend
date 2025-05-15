package com.example.jammoney.financeQuiz.dto;

import com.example.jammoney.financeQuiz.entity.QuizCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WrongNoteRequest {
    private String question;
    private String selectedOption;
    private String correctAnswer;
    private String explanation;
    private String hint;
    private QuizCategory category;
}