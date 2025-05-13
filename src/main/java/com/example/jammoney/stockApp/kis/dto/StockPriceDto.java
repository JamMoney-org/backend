package com.example.jammoney.stockApp.kis.dto;
import lombok.Data;

@Data
public class StockPriceDto {
    private Output output;

    @Data
    public static class Output {
        private String stck_prpr; // 현재가
    }
}

