package com.example.jammoney.cash.repository;

import com.example.jammoney.cash.entity.Cash;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CashRepository extends JpaRepository<Cash, Long> {

    @Query("select c from Cash c where c.user.id = :userId")
    Optional<Cash> findByUserId(@Param("userId") long userId);

}
