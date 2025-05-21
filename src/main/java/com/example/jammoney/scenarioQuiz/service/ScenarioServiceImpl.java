package com.example.jammoney.scenarioQuiz.service;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.pet.service.PetService;
import com.example.jammoney.scenarioQuiz.entity.*;
import com.example.jammoney.scenarioQuiz.gpt.GptScenarioService;
import com.example.jammoney.scenarioQuiz.gpt.dto.*;
import com.example.jammoney.scenarioQuiz.dto.*;
import com.example.jammoney.scenarioQuiz.repository.*;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioServiceImpl implements ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioStepRepository stepRepository;
    private final ScenarioPlayLogRepository playLogRepository;
    private final GptScenarioService gptScenarioService;
    private final PetService petService;

    @Override
    public ScenarioStartResponseDTO startScenario(Long scenarioId, User user) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("해당 시나리오가 존재하지 않습니다."));

        ScenarioStep firstStep = stepRepository.findByScenarioAndStepOrder(scenario, 1)
                .orElseThrow(() -> new IllegalStateException("시나리오에 첫 번째 질문이 존재하지 않습니다."));

        // GPT에게 선택지 요청 (🔥 난이도 추가)
        GptScenarioChoiceResponse gptResponse = gptScenarioService
                .generateChoices(scenario.getTitle(), firstStep.getAiMessage(), List.of(), scenario.getDifficulty())
                .block();

        return ScenarioStartResponseDTO.builder()
                .scenarioId(scenario.getId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .stepOrder(1)
                .aiMessage(firstStep.getAiMessage())
                .choices(mapToChoiceDTOs(gptResponse.getChoices()))
                .build();
    }

    @Override
    public NextStepResponseDTO nextStep(Long scenarioId, User user, String selectedChoice, List<String> history, int currentStep) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("시나리오를 찾을 수 없습니다."));

        // 기록 저장
        ScenarioPlayLog log = ScenarioPlayLog.builder()
                .scenario(scenario)
                .user(user)
                .stepOrder(currentStep)
                .choiceContent(selectedChoice)
                .selectedAt(LocalDateTime.now())
                .build();
        playLogRepository.save(log);

        // 다음 질문 생성 (🔥 난이도 추가)
        GptNextMessageResponse nextMessage = gptScenarioService
                .generateNextStep(selectedChoice, history, scenario.getDifficulty())
                .block();

        // 히스토리 업데이트
        List<String> updatedHistory = new ArrayList<>(history);
        updatedHistory.add(selectedChoice);

        // 다음 질문에 대한 선택지 생성 (🔥 난이도 추가)
        GptScenarioChoiceResponse gptChoices = gptScenarioService
                .generateChoices(scenario.getTitle(), nextMessage.getNextAiMessage(), updatedHistory, scenario.getDifficulty())
                .block();

        // 종료 선택지만 있는 경우 → 총평 + 경험치 지급
        boolean isAllEnd = gptChoices.getChoices().stream().allMatch(GptChoiceData::isEnd);
        if (isAllEnd) {
            int rewardExp = calculateRewardExp(scenario.getDifficulty());
            petService.addExp(user, rewardExp);

            return NextStepResponseDTO.builder()
                    .stepOrder(currentStep + 1)
                    .aiMessage("시나리오가 종료되었습니다.")
                    .choices(new ArrayList<>())
                    .isFinalStep(true)
                    .build();
        }

        return NextStepResponseDTO.builder()
                .stepOrder(currentStep + 1)
                .aiMessage(nextMessage.getNextAiMessage())
                .choices(mapToChoiceDTOs(gptChoices.getChoices()))
                .isFinalStep(false)
                .build();
    }

    private int calculateRewardExp(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 10;
            case NORMAL -> 20;
            case HARD -> 30;
        };
    }

    @Override
    public ScenarioEvaluationResponseDTO summarizeScenario(List<String> selectedChoices) {
        GptScenarioSummaryResponse summary = gptScenarioService.generateSummary(selectedChoices).block();
        return new ScenarioEvaluationResponseDTO(summary.getSummary());
    }

    private List<ScenarioChoiceDTO> mapToChoiceDTOs(List<GptChoiceData> choices) {
        List<ScenarioChoiceDTO> result = new ArrayList<>();
        for (int i = 0; i < choices.size(); i++) {
            GptChoiceData choice = choices.get(i);
            result.add(ScenarioChoiceDTO.builder()
                    .choiceId((long) (i + 1))
                    .content(choice.getContent())
                    .isGood(choice.isGood())
                    .isEnd(choice.isEnd())
                    .build());
        }
        return result;
    }

    // 시나리오 및 첫 질문 생성
    @Override
    public void createScenarioWithFirstStep(ScenarioCreateRequestDTO request) {
        Scenario scenario = Scenario.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .difficulty(request.getDifficulty()) // 🔥 난이도 저장
                .build();
        scenarioRepository.save(scenario);

        ScenarioStep firstStep = ScenarioStep.builder()
                .scenario(scenario)
                .stepOrder(1)
                .aiMessage(request.getFirstAiMessage())
                .isEndStep(false)
                .build();
        stepRepository.save(firstStep);
    }
}