package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<FinancialCategory, Long> {
    Optional<FinancialCategory> findByCategory(String category);
}
