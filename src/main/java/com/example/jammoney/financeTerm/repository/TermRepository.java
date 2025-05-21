package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialTerm;
import com.example.jammoney.financeTerm.entity.FinancialCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<FinancialTerm, Long> {
    List<FinancialTerm> findByCategoryAndDayIndex(FinancialCategory category, int dayIndex);
}
