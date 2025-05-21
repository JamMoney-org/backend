package com.example.jammoney.scenarioQuiz.gpt;

import com.example.jammoney.financeQuiz.entity.Difficulty;
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

    // ✅ 초기 질문 or 다음 질문에 대한 선택지 생성 (난이도 포함)
    @GetMapping("/choices")
    public Mono<GptScenarioChoiceResponse> testChoices(
            @RequestParam String topic,
            @RequestParam String question,
            @RequestParam Difficulty difficulty
    ) {
        List<String> history = List.of(); // 초기 상태
        return gptScenarioService.generateChoices(topic, question, history, difficulty);
    }

    // ✅ 사용자 선택 → 다음 질문 생성 (난이도 포함)
    @PostMapping("/next")
    public Mono<GptNextMessageResponse> testNextStep(
            @RequestBody List<String> history,
            @RequestParam String selected,
            @RequestParam Difficulty difficulty
    ) {
        return gptScenarioService.generateNextStep(selected, history, difficulty);
    }

    // ✅ 총평 생성 (그대로)
    @PostMapping("/summary")
    public Mono<GptScenarioSummaryResponse> testSummary(
            @RequestBody List<String> selectedChoices
    ) {
        return gptScenarioService.generateSummary(selectedChoices);
    }
}