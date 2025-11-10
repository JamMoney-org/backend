package com.example.jammoney.news.quiz.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuizResultDto {
    private final boolean correct;
    private final int correctAnswerIndex;
}
