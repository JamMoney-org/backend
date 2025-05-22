/*package com.example.jammoney;


import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.pet.service.PetService;
import com.example.jammoney.scenarioQuiz.dto.*;
import com.example.jammoney.scenarioQuiz.entity.*;
import com.example.jammoney.scenarioQuiz.gpt.GptScenarioService;
import com.example.jammoney.scenarioQuiz.gpt.dto.*;
import com.example.jammoney.scenarioQuiz.repository.*;
import com.example.jammoney.scenarioQuiz.service.ScenarioServiceImpl;
import com.example.jammoney.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ScenarioServiceImplTest {

    @Mock ScenarioRepository scenarioRepository;
    @Mock ScenarioStepRepository stepRepository;
    @Mock ScenarioPlayLogRepository playLogRepository;
    @Mock GptScenarioService gptScenarioService;
    @Mock PetService petService;

    @InjectMocks
    ScenarioServiceImpl scenarioService;

    Scenario scenario;
    ScenarioStep firstStep;
    User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder().id(1L).build();
        scenario = Scenario.builder().id(1L).title("자취방 계약").description("첫 자취방 찾기").difficulty(Difficulty.NORMAL).build();
        firstStep = ScenarioStep.builder().id(1L).scenario(scenario).stepOrder(1).aiMessage("어떤 조건의 자취방을 찾으시나요?").build();
    }

    @Test
    void startScenario_정상동작() {
        GptChoiceData choice = new GptChoiceData("방 찾기", "좋은 선택이에요", true, false);
        GptScenarioChoiceResponse gptResponse = new GptScenarioChoiceResponse(List.of(choice));

        when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenario));
        when(stepRepository.findByScenarioAndStepOrder(scenario, 1)).thenReturn(Optional.of(firstStep));
        when(gptScenarioService.generateChoices(anyString(), anyString(), anyList(), any(Difficulty.class))).thenReturn(Mono.just(gptResponse));

        ScenarioStartResponseDTO response = scenarioService.startScenario(1L, user);

        assertThat(response.getScenarioId()).isEqualTo(1L);
        assertThat(response.getStepOrder()).isEqualTo(1);
        assertThat(response.getChoices()).hasSize(1);
    }

    @Test
    void nextStep_종료아님() {
        GptNextMessageResponse nextMessage = new GptNextMessageResponse("햇빛은 잘 들어오나요?");
        GptChoiceData choice = new GptChoiceData("햇빛 고려", "좋은 판단이에요", true, false);
        GptScenarioChoiceResponse choiceResponse = new GptScenarioChoiceResponse(List.of(choice));

        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.of(scenario));
        when(gptScenarioService.generateNextStep(anyString(), anyList(), any(Difficulty.class))).thenReturn(Mono.just(nextMessage));
        when(gptScenarioService.generateChoices(anyString(), anyString(), anyList(), any(Difficulty.class))).thenReturn(Mono.just(choiceResponse));

        NextStepResponseDTO result = scenarioService.nextStep(1L, user, "예산 고려", List.of("예산 고려"), 1);

        assertThat(result.getStepOrder()).isEqualTo(2);
        assertThat(result.getChoices()).hasSize(1);
        assertThat(result.getAiMessage()).isEqualTo("햇빛은 잘 들어오나요?");
    }

    @Test
    void nextStep_종료선택지만존재() {
        GptNextMessageResponse nextMessage = new GptNextMessageResponse("이 방으로 계약하시겠어요?");
        GptChoiceData choice = new GptChoiceData("계약하기", "좋은 선택입니다", true, true);
        GptScenarioChoiceResponse choiceResponse = new GptScenarioChoiceResponse(List.of(choice));
        GptScenarioSummaryResponse summary = new GptScenarioSummaryResponse("신중한 판단을 하셨네요");

        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.of(scenario));
        when(gptScenarioService.generateNextStep(anyString(), anyList(), any(Difficulty.class))).thenReturn(Mono.just(nextMessage));
        when(gptScenarioService.generateChoices(anyString(), anyString(), anyList(), any(Difficulty.class))).thenReturn(Mono.just(choiceResponse));
        when(gptScenarioService.generateSummary(anyList())).thenReturn(Mono.just(summary));

        NextStepResponseDTO result = scenarioService.nextStep(1L, user, "마무리 선택", List.of("1", "2"), 3);

        assertThat(result.getAiMessage()).contains("시나리오가 종료되었습니다.");
        assertThat(result.getChoices()).isEmpty();
    }

    @Test
    void summarizeScenario_총평응답() {
        GptScenarioSummaryResponse summary = new GptScenarioSummaryResponse("매우 신중한 선택이었습니다");
        when(gptScenarioService.generateSummary(anyList())).thenReturn(Mono.just(summary));

        ScenarioEvaluationResponseDTO result = scenarioService.summarizeScenario(List.of("A", "B"));

        assertThat(result.getOverallFeedback()).isEqualTo("매우 신중한 선택이었습니다");
    }
}*/