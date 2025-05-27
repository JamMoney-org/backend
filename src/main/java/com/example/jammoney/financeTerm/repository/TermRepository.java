package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialTerm;
import com.example.jammoney.financeTerm.entity.FinancialCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<FinancialTerm, Long> {
    List<FinancialTerm> findByCategoryAndDayIndex(FinancialCategory category, int dayIndex);
    List<FinancialTerm> findByCategory(FinancialCategory category);
    Optional<FinancialTerm> findByTermAndCategory(String term, FinancialCategory category);

}
