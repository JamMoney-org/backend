package com.example.jammoney.gpt.service;


import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.FinanceQuiz;
import com.example.jammoney.financeQuiz.entity.QuizCategory;

import java.util.List;

public interface GptApiService {
    List<FinanceQuiz> requestFinanceQuizzes(QuizCategory category, Difficulty difficulty);
}