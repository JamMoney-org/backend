package com.example.jammoney.stockApp.stock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column(nullable = false, unique = true)
    private String code; // 종목 코드

    private String korName; // 회사명

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private StockAskingPrice stockAskingPrice;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private StockInfo stockInfo;

    private Long marketCap; // 시가총액 (단위: 원)

    private Long faceValue; // 액면가 (단위: 원)

    private LocalDate listedDate; // 상장일자

    private Long listedShares; // 상장 주식 수

    private String industry; // 업종 분류

    private BigDecimal dividendYield; // 배당수익률 (단위: %)

    private BigDecimal dividendPerShare; // 주당배당금 (단위: 원)

    private BigDecimal eps; // EPS (주당순이익)

    private BigDecimal per; // PER (주가수익비율)

    private BigDecimal bps; // BPS (주당순자산가치)

    private BigDecimal pbr; // PBR (주가순자산비율)

    private String settlementMonth; // 결산월 (예: 12월)
}
