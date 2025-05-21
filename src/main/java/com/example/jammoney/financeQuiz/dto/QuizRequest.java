package com.example.jammoney.financeQuiz.dto;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import lombok.Getter;

@Getter
public class QuizRequest {
    private QuizCategory category;
    private Difficulty difficulty;
}