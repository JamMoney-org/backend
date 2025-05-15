package com.example.jammoney.financeQuiz.repository;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.FinanceQuiz;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceQuizRepository extends JpaRepository<FinanceQuiz, Long> {

    // AI로 생성한 문제를 저장하거나, 미리 저장된 문제를 불러올 경우
    List<FinanceQuiz> findByCategoryAndDifficulty(QuizCategory category, Difficulty difficulty);
}