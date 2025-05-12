package com.example.jammoney.stockApp.kis.repository;
import com.example.jammoney.stockApp.kis.entity.KisToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface KisTokenRepository extends JpaRepository<KisToken, Long> {
}

