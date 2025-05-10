package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HoldingStockResponseDto {
    private String companyKorName;
    //보유 수량
    private int stockCount;
    //현재가
    private long currentPrice;
    //평가금
    private long evaluationAmount;
    //수익 금액 (평가금 - 매입금)
    private long profitAmount;
    //수익률 (수익금/매입금)*100
    private double profitRate;
    //전체 자산 대비 비중
    private double portfoliRatio;
}
