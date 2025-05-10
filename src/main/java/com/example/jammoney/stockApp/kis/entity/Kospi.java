package com.example.jammoney.stockApp.kis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Kospi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 월별 날짜
    private LocalDate date;

    // 종가 (마지막 영업일 종가)
    private int close;

    // 전월 대비 가격 차이
    private int prdyVrss;

    // 전월 대비 비율
    private double prdyCrt;

}
