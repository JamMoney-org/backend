package com.example.jammoney.financeQuiz.service;

import com.example.jammoney.financeQuiz.dto.FinanceQuiz;
import com.example.jammoney.financeQuiz.dto.QuizResult;
import com.example.jammoney.financeQuiz.dto.QuizSummaryResult;
import com.example.jammoney.financeQuiz.dto.WrongNoteRequest;
import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import com.example.jammoney.financeQuiz.entity.WrongNote;
import com.example.jammoney.financeQuiz.repository.WrongNoteRepository;
import com.example.jammoney.gpt.service.GptApiService;
import com.example.jammoney.pet.service.PetService;
import com.example.jammoney.cash.service.CashService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final GptApiService gptApiService;
    private final WrongNoteRepository wrongNoteRepository;
    private final PetService petService;
    private final CashService cashService;

    @Override
    public List<FinanceQuiz> generateQuiz(QuizCategory category, Difficulty difficulty) {
        return gptApiService.requestFinanceQuizzes(category, difficulty);
    }

    @Override
    public QuizResult submitAnswer(FinanceQuiz quiz, int userAnswerIndex) {
        boolean isCorrect = quiz.getCorrectIndex() == userAnswerIndex;
        String correctAnswer = quiz.getOptions().get(quiz.getCorrectIndex());

        return QuizResult.builder()
                .correct(isCorrect)
                .correctAnswer(correctAnswer)
                .explanation(quiz.getExplanation())
                .hint(quiz.getHint())
                .build();
    }

    @Override
    public void saveWrongNote(WrongNoteRequest request, User user) {
        WrongNote wrongNote = WrongNote.builder()
                .user(user)
                .question(request.getQuestion())
                .selectedOption(request.getSelectedOption())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .hint(request.getHint())
                .category(request.getCategory())
                .build();

        wrongNoteRepository.save(wrongNote);
    }

    @Override
    public QuizSummaryResult submitQuizSet(List<QuizResult> quizResults, User user) {
        int correctCount = (int) quizResults.stream()
                .filter(QuizResult::isCorrect)
                .count();

        boolean passed = correctCount >= 3;
        int rewardExp = passed ? 10 : 0;
        int rewardCoin = passed ? 10_000 : 0;

        if (passed) {
            petService.addExp(user, rewardExp);   // 경험치는 Pet 기준
            cashService.addCash(user.getId(), rewardCoin);
        }

        return QuizSummaryResult.builder()
                .totalQuestions(quizResults.size())
                .correctCount(correctCount)
                .rewardExp(rewardExp)
                .rewardCoin(rewardCoin)
                .passed(passed)
                .build();
    }
}