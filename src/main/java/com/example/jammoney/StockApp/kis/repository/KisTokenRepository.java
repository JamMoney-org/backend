package com.example.jammoney.StockApp.kis.repository;
import com.example.jammoney.StockApp.kis.entity.KisToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface KisTokenRepository extends JpaRepository<KisToken, Long> {
}

