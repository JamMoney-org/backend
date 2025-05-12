package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.User.User;
import com.example.jammoney.financeTerm.entity.FinancialTerm;
import com.example.jammoney.financeTerm.entity.UserTermLearning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLearningRepository extends JpaRepository<UserTermLearning, Long> {
    boolean existsByUserAndTerm(User user, FinancialTerm term); // 단어를 학습했는지 여부 확인
    long countByUserAndLearnedTrue(User user);  // 전체 학습 완료한 단어 개수 (학습률)
    List<UserTermLearning> findByUserAndTermIn(User user, List<FinancialTerm> terms);// Day 단위로 학습된 단어들 조회 (경험치 지급)
}
