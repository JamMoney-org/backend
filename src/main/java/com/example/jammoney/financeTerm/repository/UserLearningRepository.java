package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialTerm;
import com.example.jammoney.financeTerm.entity.FinancialTermQuiz;
import com.example.jammoney.financeTerm.entity.QuizSubmission;
import com.example.jammoney.financeTerm.entity.UserTermLearning;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLearningRepository extends JpaRepository<UserTermLearning, Long> {
    boolean existsByUserAndTerm(User user, FinancialTerm term);
    long countByUserAndLearnedTrue(User user);
    List<UserTermLearning> findByUserAndTermIn(User user, List<FinancialTerm> terms);
}
