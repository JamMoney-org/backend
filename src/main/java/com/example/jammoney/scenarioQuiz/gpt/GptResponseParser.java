package com.example.jammoney.scenarioQuiz.gpt;

import com.example.jammoney.scenarioQuiz.gpt.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GptResponseParser {

    private final ObjectMapper objectMapper;

    // 1️⃣ 선택지 + 피드백 파싱
    public GptScenarioChoiceResponse parseChoiceResponse(GptChatResponse raw) {
        String content = raw.getChoices().get(0).getMessage().getContent();

        try {
            List<GptChoiceData> choices = objectMapper.readValue(
                    content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GptChoiceData.class)
            );
            return GptScenarioChoiceResponse.builder()
                    .choices(choices)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("선택지 파싱 실패: " + content, e);
        }
    }

    // 2️⃣ 다음 질문 파싱
    public GptNextMessageResponse parseNextMessage(GptChatResponse raw) {
        String message = raw.getChoices().get(0).getMessage().getContent();
        return new GptNextMessageResponse(message.trim()); // 순수 문자열
    }

    // 3️⃣ 총평 파싱
    public GptScenarioSummaryResponse parseSummary(GptChatResponse raw) {
        String summary = raw.getChoices().get(0).getMessage().getContent();
        return new GptScenarioSummaryResponse(summary.trim());
    }
}