package com.example.jammoney.financeQuiz.service;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.FinanceQuiz;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import com.example.jammoney.gpt.service.GptApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GptQuizGenerator {

    private final GptApiService gptApiService;

    public List<FinanceQuiz> generateQuizList(QuizCategory category, Difficulty difficulty) {
        // GPT에게 요청 (prompt는 내부에서 생성됨)
        return gptApiService.requestFinanceQuizzes(category, difficulty);
    }
}