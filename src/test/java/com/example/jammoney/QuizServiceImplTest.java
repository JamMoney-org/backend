package com.example.jammoney;

import com.example.jammoney.financeQuiz.dto.*;
import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import com.example.jammoney.financeQuiz.entity.WrongNote;
import com.example.jammoney.financeQuiz.repository.WrongNoteRepository;
import com.example.jammoney.financeQuiz.service.QuizServiceImpl;
import com.example.jammoney.gpt.service.GptApiService;
import com.example.jammoney.pet.service.PetService;
import com.example.jammoney.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class QuizServiceImplTest {

    @InjectMocks
    private QuizServiceImpl quizService;

    @Mock
    private GptApiService gptApiService;

    @Mock
    private WrongNoteRepository wrongNoteRepository;

    @Mock
    private PetService petService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateQuiz_정상동작() {
        List<FinanceQuiz> mockQuizzes = List.of(
                FinanceQuiz.builder().question("Q1").build(),
                FinanceQuiz.builder().question("Q2").build()
        );

        when(gptApiService.requestFinanceQuizzes(QuizCategory.CONSUMPTION, Difficulty.EASY))
                .thenReturn(mockQuizzes);

        List<FinanceQuiz> result = quizService.generateQuiz(QuizCategory.CONSUMPTION, Difficulty.EASY);

        assertThat(result).hasSize(2);
        verify(gptApiService).requestFinanceQuizzes(QuizCategory.CONSUMPTION, Difficulty.EASY);
    }

    @Test
    void submitAnswer_정답일때() {
        FinanceQuiz quiz = FinanceQuiz.builder()
                .question("What is 2+2?")
                .options(List.of("3", "4", "5"))
                .correctIndex(1)
                .explanation("2+2는 4입니다.")
                .hint("덧셈")
                .build();

        QuizResult result = quizService.submitAnswer(quiz, 1);

        assertThat(result.isCorrect()).isTrue();
        assertThat(result.getCorrectAnswer()).isEqualTo("4");
    }

    @Test
    void submitAnswer_오답일때() {
        FinanceQuiz quiz = FinanceQuiz.builder()
                .question("What is 2+2?")
                .options(List.of("3", "4", "5"))
                .correctIndex(1)
                .explanation("2+2는 4입니다.")
                .hint("덧셈")
                .build();

        QuizResult result = quizService.submitAnswer(quiz, 0);

        assertThat(result.isCorrect()).isFalse();
        assertThat(result.getCorrectAnswer()).isEqualTo("4");
    }

    @Test
    void saveWrongNote_저장성공() {
        User user = User.builder().id(1L).build();

        WrongNoteRequest request = new WrongNoteRequest();
        request.setQuestion("질문");
        request.setSelectedOption("내 선택");
        request.setCorrectAnswer("정답");
        request.setExplanation("해설");
        request.setHint("힌트");
        request.setCategory(QuizCategory.SAVING);

        quizService.saveWrongNote(request, user);

        ArgumentCaptor<WrongNote> captor = ArgumentCaptor.forClass(WrongNote.class);
        verify(wrongNoteRepository).save(captor.capture());

        WrongNote saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getQuestion()).isEqualTo("질문");
    }

    @Test
    void submitQuizSet_보상지급() {
        User user = User.builder().id(1L).coin(0).build();

        List<QuizResult> quizResults = List.of(
                QuizResult.builder().correct(true).build(),
                QuizResult.builder().correct(true).build(),
                QuizResult.builder().correct(true).build(),
                QuizResult.builder().correct(false).build(),
                QuizResult.builder().correct(false).build()
        );

        QuizSummaryResult result = quizService.submitQuizSet(quizResults, user);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRewardExp()).isEqualTo(10);
        assertThat(result.getRewardCoin()).isEqualTo(10000);
        verify(petService).addExp(eq(user), eq(10));
    }
}