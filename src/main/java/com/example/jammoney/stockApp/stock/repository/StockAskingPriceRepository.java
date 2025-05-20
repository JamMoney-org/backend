package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAskingPriceRepository extends JpaRepository<StockAskingPrice, Long> {
    StockAskingPrice findByCompany_CompanyId(Long companyId);
}