package com.example.jammoney.stockApp.stock.dto;

import lombok.Data;

@Data
public class KospiResponseDto {
    //현재 달 (YYYYMM)
    private String date;

    //시가
    private double open;

    //고가
    private double high;

    //저가
    private double low;

    //종가
    private double close;
}
