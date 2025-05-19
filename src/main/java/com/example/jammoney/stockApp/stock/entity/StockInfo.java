package com.example.jammoney.stockApp.stock.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StockInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockInfoId;

    @OneToOne
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    //주식 현재가
    private String stck_prpr;

    //전일 대비
    private String prdy_vrss;

    //전일 대비율
    private String prdy_ctrt;

    //누적 거래량
    private String acml_vol;

    //누적 거래대금
    private String acml_tr_pbmn;
}
