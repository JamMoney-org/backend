package com.example.Jammoney.StockApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockId;

    @Column(nullable = false, unique = true)
    private String stockCode; // 예: "005930"

    @Column(nullable = false)
    private String stockName;

    private LocalDate date;

    private Long startPrice;
    private Long highPrice;
    private Long lowPrice;
    private Long currentPrice;

    private Double fluctuationRate; // 등락률 (예: -1.25)

    private Long volume;        // 거래량
    private Long tradingValue;  // 거래대금
    private Long marketCap;     // 시가총액
}
