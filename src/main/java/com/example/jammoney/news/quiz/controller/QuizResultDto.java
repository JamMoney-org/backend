package com.example.jammoney.news.quiz.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

//퀴즈 제출 결과 (정답여부와 정답 공개)
@Getter
@AllArgsConstructor
public class QuizResultDto {
    private final boolean correct;

    private final int correctAnswerIndex;
}
