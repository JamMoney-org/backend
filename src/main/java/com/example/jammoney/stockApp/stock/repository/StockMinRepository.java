package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.StockMin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockMinRepository extends JpaRepository<StockMin, Long> {
    List<StockMin> findAllByCompanyCompanyId(Long companyId);
    @Query(value = "SELECT * FROM stock_min s WHERE s.company_id = ?1 ORDER BY s.stock_min_id DESC LIMIT 420", nativeQuery = true)
    List<StockMin> findTop420ByCompanyIdOrderByStockMinIdDesc(Long companyId);
}