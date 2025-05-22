package com.example.jammoney.stockApp.kis.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KospiRepository extends JpaRepository<Kospi, Long> {
    Optional<Object> findByDate(LocalDate date);
    List<Kospi> findAllByOrderByDateAsc();
}