package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.Company;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
  Optional<Company> findByCode(@NotBlank(message = "종목 코드는 필수입니다.") String companyCode);
}