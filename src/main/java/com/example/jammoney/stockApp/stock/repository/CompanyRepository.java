package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.Company;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
  @Query("SELECT c FROM Company c JOIN FETCH c.stockAskingPrice JOIN FETCH c.stockInfo WHERE c.code = :code")
  Company findByCode(@Param("code") String code);
  @Query("SELECT c FROM Company c JOIN FETCH c.stockAskingPrice JOIN FETCH c.stockInfo WHERE c.companyId = :companyId")
  Company findByCompanyId(@Param("companyId") long companyId);
  @Query("SELECT c from Company c join fetch c.stockAskingPrice JOIN FETCH c.stockInfo")
  List<Company> findAll();
  boolean existsByCode(String code);

}