package com.example.jammoney.financeQuiz.dto;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizStartRequestDTO {
    private QuizCategory category;
    private Difficulty difficulty;
}