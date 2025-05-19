package com.example.jammoney.stockApp.stock.dto;

import lombok.Data;

@Data
public class KospiResponseDto {
    private String date;       // YYYYMM
    private double open;
    private double high;
    private double low;
    private double close;
}
