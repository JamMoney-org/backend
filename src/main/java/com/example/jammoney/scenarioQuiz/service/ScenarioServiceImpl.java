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
    public NextStepResponseDTO nextStep(Long scenarioId, User user, String selectedChoice, int currentStep) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("시나리오를 찾을 수 없습니다."));

        String prevAiMessage;

        // ✅ 1️⃣ 이전 질문 가져오기 (1단계는 ScenarioStep에서, 이후는 PlayLog에서)
        if (currentStep == 1) {
            ScenarioStep firstStep = stepRepository.findByScenarioAndStepOrder(scenario, 1)
                    .orElseThrow(() -> new IllegalStateException("시나리오의 첫 질문이 없습니다."));
            prevAiMessage = firstStep.getAiMessage();
        } else {
            ScenarioPlayLog prevLog = playLogRepository.findByScenarioAndUserAndStepOrder(scenario, user, currentStep)
                    .orElseThrow(() -> new IllegalStateException("이전 질문 로그가 없습니다."));
            prevAiMessage = prevLog.getAiMessage();
        }

        // ✅ 2️⃣ 현재 선택 저장
        ScenarioPlayLog log = ScenarioPlayLog.builder()
                .scenario(scenario)
                .user(user)
                .stepOrder(currentStep + 1)
                .choiceContent(selectedChoice)
                .selectedAt(LocalDateTime.now())
                .aiMessage(prevAiMessage)
                .build();
        playLogRepository.save(log);

        // ✅ 3️⃣ 전체 선택 이력 조회
        List<ScenarioPlayLog> playLogs = playLogRepository.findByScenarioAndUserOrderByStepOrderAsc(scenario, user);
        List<String> history = playLogs.stream()
                .map(ScenarioPlayLog::getChoiceContent)
                .toList();

        // ✅ 4️⃣ 다음 질문 생성
        GptNextMessageResponse nextMessage = gptScenarioService
                .generateNextStep(prevAiMessage, selectedChoice, history, scenario.getDifficulty())
                .block();

        // ✅ 5️⃣ 다음 선택지 생성
        GptScenarioChoiceResponse gptChoices = gptScenarioService
                .generateChoices(scenario.getTitle(), nextMessage.getNextAiMessage(), history, scenario.getDifficulty())
                .block();

        // ✅ 6️⃣ 종료 판단 및 보상
        boolean isAllEnd = gptChoices.getChoices().stream().allMatch(GptChoiceData::isEnd);
        if (isAllEnd) {
            int rewardExp = calculateRewardExp(scenario.getDifficulty());
            petService.addExp(user, rewardExp);

            return NextStepResponseDTO.builder()
                    .stepOrder(currentStep + 1)
                    .aiMessage("시나리오가 종료되었습니다.")
                    .choices(new ArrayList<>())
                    .build();
        }

        // ✅ 7️⃣ 응답 반환
        return NextStepResponseDTO.builder()
                .stepOrder(currentStep + 1)
                .aiMessage(nextMessage.getNextAiMessage())
                .choices(mapToChoiceDTOs(gptChoices.getChoices()))
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

    @Override
    public void createScenarioWithFirstStep(ScenarioCreateRequestDTO request) {
        Scenario scenario = Scenario.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
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