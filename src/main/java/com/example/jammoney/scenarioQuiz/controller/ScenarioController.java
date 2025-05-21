package com.example.jammoney.scenarioQuiz.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.scenarioQuiz.dto.*;
import com.example.jammoney.scenarioQuiz.service.ScenarioService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenario")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    // 시나리오 생성 요청 (관리자용)
    @PostMapping("/create")
    public String createScenario(@RequestBody ScenarioCreateRequestDTO request) {
        scenarioService.createScenarioWithFirstStep(request);
        return "시나리오 및 첫 질문이 생성되었습니다.";
    }

    // 1. 시나리오 시작 요청
    @PostMapping("/start")
    public ScenarioStartResponseDTO startScenario(
            @RequestBody ScenarioStartRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return scenarioService.startScenario(request.getScenarioId(), user);
    }

    // 2. 다음 스텝 진행
    @PostMapping("/next")
    public NextStepResponseDTO nextStep(
            @RequestBody ScenarioChoiceRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return scenarioService.nextStep(
                request.getScenarioId(),
                user,
                request.getSelectedChoice(),
                request.getCurrentStep()
        );
    }

    // 3. 총평 요청
    @PostMapping("/summary")
    public ScenarioEvaluationResponseDTO summarize(
            @RequestBody List<String> selectedChoices) {
        return scenarioService.summarizeScenario(selectedChoices);
    }
}