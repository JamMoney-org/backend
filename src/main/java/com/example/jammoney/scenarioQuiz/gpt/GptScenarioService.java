package com.example.jammoney.scenarioQuiz.gpt;

import com.example.jammoney.scenarioQuiz.entity.ScenarioPlayLog;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptChoiceData;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptNextMessageResponse;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptScenarioChoiceResponse;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptScenarioSummaryResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GptScenarioService {
    Mono<GptScenarioChoiceResponse> generateChoices(String topic, String aiMessage, List<String> history);

    Mono<GptNextMessageResponse> generateNextStep(String selectedChoice, List<String> history);

    Mono<GptScenarioSummaryResponse> generateSummary(List<String> selectedChoices);
}