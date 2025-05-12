package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialTerm;
import com.example.jammoney.financeTerm.entity.UserSavedTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedTermRepository extends JpaRepository<UserSavedTerm, Long> {
    List<UserSavedTerm> findAllByUser(User user); // 사용자가 북마크한 단어 전체 조회
    Optional<UserSavedTerm> findByUserAndTerm(User user, FinancialTerm term); // 단어가 북마크되어 있는지 확인
}