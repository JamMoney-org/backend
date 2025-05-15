package com.example.jammoney.financeQuiz.dto;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceQuiz {
    private String question;
    private List<String> options;
    private int correctIndex;
    private String hint;
    private String explanation;
    private Difficulty difficulty;
    private QuizCategory category;
}