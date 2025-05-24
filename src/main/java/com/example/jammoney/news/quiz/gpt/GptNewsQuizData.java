package com.example.jammoney.news.quiz.gpt;

import java.util.List;

public record GptNewsQuizData(
        String question,
        List<String> options,
        int answerIndex
) {}
