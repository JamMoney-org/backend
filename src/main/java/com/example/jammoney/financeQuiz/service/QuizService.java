package com.example.jammoney.financeQuiz.service;

import com.example.jammoney.financeQuiz.dto.FinanceQuiz;
import com.example.jammoney.financeQuiz.dto.QuizResult;
import com.example.jammoney.financeQuiz.dto.QuizSummaryResult;
import com.example.jammoney.financeQuiz.dto.WrongNoteRequest;
import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import com.example.jammoney.user.entity.User;

import java.util.List;

public interface QuizService {
    List<FinanceQuiz> generateQuiz(QuizCategory category, Difficulty difficulty);
    QuizResult submitAnswer(FinanceQuiz quiz, int userAnswerIndex);
    void saveWrongNote(WrongNoteRequest request, User user);
    QuizSummaryResult submitQuizSet(List<QuizResult> quizResults, User user);
}