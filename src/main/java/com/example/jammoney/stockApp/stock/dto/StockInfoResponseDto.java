package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
/**
 * 종목의 실시간 시세 정보 응답 DTO
 * - 현재가, 전일 대비, 거래량/거래대금 포함
 * - 종목 리스트/상세화면, 관심종목 등에 사용
 */

public class StockInfoResponseDto {
    private String stockCode;        // 종목 코드
    private String stockName;        // 종목 이름
    private String currentPrice;     // 현재가
    private String priceDiff;        // 전일 대비
    private String priceDiffRate;    // 전일 대비율
    private String accumulatedVolume;    // 누적 거래량
    private String accumulatedAmount;    // 누적 거래대금
}
