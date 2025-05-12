package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<FinancialTerm, Long> {
    List<FinancialTerm> findByCategoryAndDayIndex(String category, int dayIndex); // 카테고리 + Day 묶음으로 5개 단어 조회
}
