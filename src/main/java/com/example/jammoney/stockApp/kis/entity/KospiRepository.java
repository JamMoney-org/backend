package com.example.jammoney.stockApp.kis.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KospiRepository extends JpaRepository<Kospi, Long> {
    List<Kospi> findAllByOrderByDateAsc();
}