package com.example.jammoney.stockApp.kis.dto;
import lombok.Data;

@Data
/**
 * [KIS API] 주식 현재가 조회 응답 DTO
 * - 단일 종목의 현재가(stck_prpr)만을 포함
 * - 종목 코드로 현재가 조회 시 응답값을 매핑
 */

public class StockPriceDto {
    private Output output;

    @Data
    public static class Output {
        private String stck_prpr; // 현재가
    }
}

