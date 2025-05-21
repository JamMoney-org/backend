package com.example.jammoney.financeQuiz.gpt.dto;


import lombok.Getter;

import java.util.List;

@Getter
public class GptQuizResponse {
    private List<QuizData> quizzes;

    @Getter
    public static class QuizData {
        private String question;
        private List<String> options;
        private int correctIndex;
        private String hint;
        private String explanation;
        private String difficulty;
        private String category;
    }
}