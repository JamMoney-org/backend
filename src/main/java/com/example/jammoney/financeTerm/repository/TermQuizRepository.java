package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialTerm;
import com.example.jammoney.financeTerm.entity.FinancialTermQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermQuizRepository extends JpaRepository<FinancialTermQuiz, Long> {
    List<FinancialTermQuiz> findByTerm(FinancialTerm term);
}
