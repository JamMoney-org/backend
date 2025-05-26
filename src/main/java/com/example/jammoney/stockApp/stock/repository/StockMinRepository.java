package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.StockMin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMinRepository extends JpaRepository<StockMin, Long> {
    List<StockMin> findAllByCompany_CompanyId(Long companyId);
    @Query(value = "SELECT * FROM stock_min s WHERE s.company_id = ?1 ORDER BY s.stock_min_id DESC LIMIT 420", nativeQuery = true)
    List<StockMin> findTop420ByCompanyIdOrderByStockMinIdDesc(Long companyId);

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO stock_min 
        (COMPANY_ID, stock_trade_time, stck_cntg_hour, stck_prpr, stck_oprc, stck_hgpr, stck_lwpr, cntg_vol)
        VALUES 
        (:companyId, :stockTradeTime, :stckCntgHour, :stckPrpr, :stckOprc, :stckHgpr, :stckLwpr, :cntgVol)
        """, nativeQuery = true)
    void insertIgnore(
            @Param("companyId") Long companyId,
            @Param("stockTradeTime") LocalDateTime stockTradeTime,
            @Param("stckCntgHour") String stckCntgHour,
            @Param("stckPrpr") String stckPrpr,
            @Param("stckOprc") String stckOprc,
            @Param("stckHgpr") String stckHgpr,
            @Param("stckLwpr") String stckLwpr,
            @Param("cntgVol") String cntgVol
    );
}