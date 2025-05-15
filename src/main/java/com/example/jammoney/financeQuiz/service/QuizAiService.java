package com.example.jammoney.financeQuiz.service;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.FinanceQuiz;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import com.example.jammoney.financeQuiz.repository.FinanceQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizAiService {

    private final GptQuizGenerator gptQuizGenerator;
    private final FinanceQuizRepository quizRepository;

    // GPT로 퀴즈 5개 생성
    public List<FinanceQuiz> generateQuizzesWithGpt(QuizCategory category, Difficulty difficulty) {
        return gptQuizGenerator.generateQuizList(category, difficulty);
    }
}