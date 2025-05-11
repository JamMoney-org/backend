package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingStockRepository extends JpaRepository<HoldingStock, Long> {
    Optional<HoldingStock> findByUserAndCompany(User user, Company company);
    List<HoldingStock> findByUser(User user);
}