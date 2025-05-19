package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPortfolioResponseDto {

    // 사용자 기본 정보
    private String nickname;

    //보유 현금
    private long cash;

    // 보유 주식 자산 총액
    private long stockAsset;

    // 총 평가 자산 = 현금 + 주식
    private long totalAsset;

    // 수익금 = 총 평가 자산 - 투자원금
    private long profitAmount;

    // 수익률 (%)
    private double profitRate;
}
