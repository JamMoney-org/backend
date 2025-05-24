package com.example.jammoney.news.quiz.service;

import com.example.jammoney.news.dto.NewsQuizDto;
import com.example.jammoney.news.entity.NewsQuiz;
import com.example.jammoney.news.quiz.controller.QuizSubmissionDto;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.news.quiz.controller.QuizResultDto;

public interface NewsQuizService {
    //GPT를 호출해 해당 뉴스 콘텐츠로부터 퀴즈를 생성·저장 후 DTO로 반환
    NewsQuizDto generateAndSaveQuiz(Long newsId);
    boolean checkAnswer(Long quizId, int selectedOptionIndex);
    NewsQuiz findQuizById(Long quizId);
    QuizResultDto submitQuiz(Long newsId, QuizSubmissionDto submission, User user);
}
