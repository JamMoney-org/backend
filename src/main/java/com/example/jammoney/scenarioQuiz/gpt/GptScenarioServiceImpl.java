package com.example.jammoney.scenarioQuiz.gpt;

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

    /**
     * 질문에 대한 선택지 + 피드백 생성
     * (초기 질문이든 다음 질문이든 공통)
     */
    @Override
    public Mono<GptScenarioChoiceResponse> generateChoices(String topic, String aiMessage, List<String> history) {
        String prompt = promptBuilder.buildChoicesPrompt(topic, aiMessage, history);

        return gptApiClient.callGpt(prompt)
                .map(responseParser::parseChoiceResponse);
    }

    /**
     * 선택 이후 → 그 선택에 따라 이어지는 다음 질문(한 문장) 생성
     */
    @Override
    public Mono<GptNextMessageResponse> generateNextStep(String selectedChoice, List<String> history) {
        String prompt = promptBuilder.buildNextMessagePrompt(selectedChoice, history);

        return gptApiClient.callGpt(prompt)
                .map(responseParser::parseNextMessage);
    }

    /**
     * 전체 선택 이력 기반 총평 생성
     */
    @Override
    public Mono<GptScenarioSummaryResponse> generateSummary(List<String> selectedChoices) {
        String prompt = promptBuilder.buildSummaryPrompt(selectedChoices);

        return gptApiClient.callGpt(prompt)
                .map(responseParser::parseSummary);
    }
}