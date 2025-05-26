package com.example.jammoney.news.quiz.controller;

import lombok.Getter;
import lombok.Setter;

//퀴즈 정답 제출용 DTO (사용자가 선택한 답)
@Getter
@Setter
public class QuizSubmissionDto {
    private Long quizId;
    private int selectedIndex;
}
