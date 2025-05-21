package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HoldingStockRepository extends JpaRepository<HoldingStock, Long> {
    @Query("SELECT h FROM HoldingStock h WHERE h.company.companyId = :companyId AND h.user.id = :userId")
    HoldingStock findByCompanyAndUser(@Param("companyId") Long companyId,
                                                    @Param("userId") Long userId);
    @Query("SELECT h FROM HoldingStock h WHERE h.user.id = :userId")
    List<HoldingStock> findByUser(@Param("userId") Long userId);
}