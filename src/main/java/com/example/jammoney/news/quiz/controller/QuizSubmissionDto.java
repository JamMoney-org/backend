package com.example.jammoney.news.quiz.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmissionDto {
    private Long quizId;
    private int selectedIndex;
}
