package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialTermQuiz;
import com.example.jammoney.financeTerm.entity.QuizSubmission;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    List<QuizSubmission> findByUser(User user);
    Optional<QuizSubmission> findByUserAndQuiz(User user, FinancialTermQuiz quiz);

}
