package com.example.jammoney.scenarioQuiz.gpt;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptNextMessageResponse;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptScenarioChoiceResponse;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptScenarioSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GptScenarioServiceImpl implements GptScenarioService {

    private final GptApiClient gptApiClient;
    private final GptPromptBuilder promptBuilder;
    private final GptResponseParser responseParser;

    // 질문에 대한 선택지 + 피드백 생성
    @Override
    public Mono<GptScenarioChoiceResponse> generateChoices(String topic, String aiMessage, List<String> history, Difficulty difficulty) {
        String prompt = promptBuilder.buildChoicesPrompt(topic, aiMessage, history, difficulty);
        return gptApiClient.callGpt(prompt)
                .map(responseParser::parseChoiceResponse);
    }

    // 선택 이후 → 전체 대화 흐름 기반 다음 질문 생성
    @Override
    public Mono<GptNextMessageResponse> generateNextStep(String conversationHistory, String selectedChoice, Difficulty difficulty) {
        String prompt = promptBuilder.buildNextMessagePrompt(conversationHistory, selectedChoice, difficulty);
        return gptApiClient.callGpt(prompt)
                .map(responseParser::parseNextMessage);
    }

    // 총평 생성
    @Override
    public Mono<GptScenarioSummaryResponse> generateSummary(List<String> selectedChoices) {
        String prompt = promptBuilder.buildSummaryPrompt(selectedChoices);
        return gptApiClient.callGpt(prompt)
                .map(responseParser::parseSummary);
    }
}