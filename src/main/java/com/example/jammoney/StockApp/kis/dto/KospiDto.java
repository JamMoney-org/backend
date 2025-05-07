package com.example.jammoney.StockApp.kis.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class KospiDto {

    private LocalDate date;
    private int close;
    private int prdyVrss;
    private double prdyCrt;
}
