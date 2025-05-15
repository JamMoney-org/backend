package com.example.jammoney.gpt.mapper;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.FinanceQuiz;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import com.example.jammoney.gpt.dto.GptQuizResponse;
import com.example.jammoney.gpt.dto.GptResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

public class GptQuizMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<FinanceQuiz> toQuizList(GptResponse response) {
        try {
            String content = response.getContent(); // GPT 응답의 JSON 문자열

            // 1단계: JSON → GptQuizResponse 객체로 파싱
            GptQuizResponse gptQuizResponse = objectMapper.readValue(
                    content,
                    GptQuizResponse.class
            );

            // 2단계: 내부 quizzes 리스트 변환
            return gptQuizResponse.getQuizzes().stream().map(dto -> FinanceQuiz.builder()
                    .question(dto.getQuestion())
                    .options(dto.getOptions())
                    .correctIndex(dto.getCorrectIndex())
                    .hint(dto.getHint())
                    .explanation(dto.getExplanation())
                    .difficulty(Difficulty.valueOf(dto.getDifficulty().toUpperCase()))
                    .category(QuizCategory.valueOf(dto.getCategory().toUpperCase()))
                    .build()
            ).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }
    }
}