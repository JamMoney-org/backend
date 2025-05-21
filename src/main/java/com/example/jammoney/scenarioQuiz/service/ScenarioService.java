package com.example.jammoney.scenarioQuiz.service;

import com.example.jammoney.scenarioQuiz.dto.*;
import com.example.jammoney.user.entity.User;

import java.util.List;

public interface ScenarioService {

    /**
     * 시나리오 시작 요청 (첫 질문 및 선택지 제공)
     */
    ScenarioStartResponseDTO startScenario(Long scenarioId, User user);

    /**
     * 사용자의 선택을 기반으로 다음 질문 및 선택지를 생성
     * 필요 시 종료 판단 및 총평 반환 포함
     */
    NextStepResponseDTO nextStep(Long scenarioId, User user, String selectedChoice, List<String> history, int currentStep);

    /**
     * 전체 선택 이력을 기반으로 시나리오 총평 요청
     */
    ScenarioEvaluationResponseDTO summarizeScenario(List<String> selectedChoices);

    void createScenarioWithFirstStep(ScenarioCreateRequestDTO request);
}
