package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockAskingPriceRepository extends JpaRepository<StockAskingPrice, Long> {
    StockAskingPrice findByCompanyCompanyId(Long stockId);
}