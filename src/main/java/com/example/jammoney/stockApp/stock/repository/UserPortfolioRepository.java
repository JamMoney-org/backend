package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.UserPortfolio;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolio, Long> {
    UserPortfolio findByUser(User user);
}
