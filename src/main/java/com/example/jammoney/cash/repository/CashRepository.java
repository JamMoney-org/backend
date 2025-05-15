package com.example.jammoney.cash.repository;

import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashRepository extends JpaRepository<Cash, Long> {
    Optional<Cash> findByUser(User user);
    Optional<Cash> findByUserId(Long userId);
}