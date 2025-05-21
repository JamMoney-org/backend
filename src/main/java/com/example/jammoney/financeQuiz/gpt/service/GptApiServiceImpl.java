package com.example.jammoney.financeQuiz.gpt.service;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.dto.FinanceQuiz;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import com.example.jammoney.financeQuiz.gpt.dto.GptResponse;
import com.example.jammoney.financeQuiz.gpt.mapper.GptQuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GptApiServiceImpl implements GptApiService {

    private final WebClient webClient;

    @Value("${gpt.api.key}")
    private String apiKey;

    @Override
    public List<FinanceQuiz> requestFinanceQuizzes(QuizCategory category, Difficulty difficulty) {
        String prompt = buildPrompt(category, difficulty);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo", // 또는 gpt-3.5-turbo 등
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 금융 퀴즈 생성 AI야. 사용자의 요구에 따라 객관식/ox 퀴즈를 만들어줘."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        GptResponse response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GptResponse.class)
                .block();

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("GPT 응답이 비어있습니다.");
        }

        return GptQuizMapper.toQuizList(response);
    }

    private String buildPrompt(QuizCategory category, Difficulty difficulty) {
        return String.format("""
            카테고리: %s
            난이도: %s

            아래 형식에 맞춰 객관식 또는 OX 문제 형태로 금융 퀴즈 5개를 만들어줘.
            각 퀴즈는 다음 정보를 포함해야 해:
            - question: 질문
            - options: 보기 리스트 (객관식은 최대 4개, OX는 2개)
            - correctIndex: 정답 인덱스 (0부터 시작)
            - hint: 문제에 대한 힌트
            - explanation: 해설
            - difficulty: EASY / NORMAL / HARD 중 하나
            - category: 소비 / 저축 / 대출 등 (영문 ENUM 형태)
            결과는 JSON 배열 형식으로만 보내줘. """, category.name(), difficulty.name());
    }
}