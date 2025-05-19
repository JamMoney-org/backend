package com.example.jammoney.financeQuiz.dto;

import com.example.jammoney.financeQuiz.entity.QuizCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WrongNoteRequest {
    private String question; //질문
    private String selectedOption; //선택된선지
    private String correctAnswer; //정답선지
    private String explanation;
    private String hint;
    private QuizCategory category;
}