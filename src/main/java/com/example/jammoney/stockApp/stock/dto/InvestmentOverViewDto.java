package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InvestmentOverViewDto {
    private String userName;         // 사용자 이름
    private long totalAsset;         // 총 평가 자산 (보유 현금 + 보유 주식 평가 금액)
    private long cash;               // 보유 현금
    private long stockAsset;         // 보유 주식 평가 금액
    private long profitAmount;       // 평가 수익 금액
    private double profitRate;       // 누적 수익률 (예: +3.24)
}
