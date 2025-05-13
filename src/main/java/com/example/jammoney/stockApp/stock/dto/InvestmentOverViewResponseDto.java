package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
/**
 * 사용자의 전체 투자 현황 요약 응답 DTO
 * - 총자산(현금 + 주식), 수익금, 수익률 등을 포함
 * - 대시보드 상단 또는 요약 카드 영역에 사용
 */

public class InvestmentOverViewResponseDto {
    private String userName;         // 사용자 이름
    private long totalAsset;         // 총 평가 자산 (보유 현금 + 보유 주식 평가 금액)
    private long cash;               // 보유 현금
    private long stockAsset;         // 보유 주식 평가 금액
    private long profitAmount;       // 평가 수익 금액
    private double profitRate;       // 누적 수익률 (예: +3.24)
}
