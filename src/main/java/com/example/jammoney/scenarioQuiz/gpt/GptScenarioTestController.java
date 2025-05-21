package com.example.jammoney.scenarioQuiz.gpt;

import com.example.jammoney.scenarioQuiz.gpt.dto.GptNextMessageResponse;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptScenarioChoiceResponse;
import com.example.jammoney.scenarioQuiz.gpt.dto.GptScenarioSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/test/gpt")
@RequiredArgsConstructor
public class GptScenarioTestController {

    private final GptScenarioService gptScenarioService;

    // 1️⃣ 초기 질문 or 다음 질문에 대한 선택지 생성
    @GetMapping("/choices")
    public Mono<GptScenarioChoiceResponse> testChoices(
            @RequestParam String topic,
            @RequestParam String question
    ) {
        List<String> history = List.of(); // 초기 상태
        return gptScenarioService.generateChoices(topic, question, history);
    }

    // 2️⃣ 사용자 선택 → 다음 질문 생성
    @PostMapping("/next")
    public Mono<GptNextMessageResponse> testNextStep(
            @RequestBody List<String> history,
            @RequestParam String selected
    ) {
        return gptScenarioService.generateNextStep(selected, history);
    }

    // 3️⃣ 전체 선택 흐름에 대한 총평 생성
    @PostMapping("/summary")
    public Mono<GptScenarioSummaryResponse> testSummary(
            @RequestBody List<String> selectedChoices
    ) {
        return gptScenarioService.generateSummary(selectedChoices);
    }
}