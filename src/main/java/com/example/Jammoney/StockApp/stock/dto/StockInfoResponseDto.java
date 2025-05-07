package com.example.Jammoney.StockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockInfoResponseDto {
    private String stockCode;        // 종목 코드
    private String stockName;        // 종목 이름
    private String currentPrice;     // 현재가
    private String priceDiff;        // 전일 대비
    private String priceDiffRate;    // 전일 대비율
    private String accumulatedVolume;    // 누적 거래량
    private String accumulatedAmount;    // 누적 거래대금
}
