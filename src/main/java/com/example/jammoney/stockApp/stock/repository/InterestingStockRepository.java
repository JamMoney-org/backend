package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.InterestingStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InterestingStockRepository extends JpaRepository<InterestingStock, Long> {
    @Query("SELECT i FROM InterestingStock i JOIN FETCH i.company WHERE i.user.id = :userId")
    List<InterestingStock> findAllByUser_Id(Long userId);

    InterestingStock findByUser_IdAndCompany_CompanyId(Long userId, Long companyId);
}
