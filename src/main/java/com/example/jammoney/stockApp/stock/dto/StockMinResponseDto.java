package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
/**
 * 특정 종목의 1분봉 등 단기 체결 시세 응답 DTO
 * - 시간별 시가, 고가, 저가, 종가, 거래량 포함
 * - 분봉 차트 데이터 구성에 사용
 */

public class StockMinResponseDto {
    private long stockMinId;
    private long companyId;
    private LocalDateTime stockTradeTime;  // 변환된 시간 정보

    private String stckCntgHour;  // 체결 시간 HHMMSS
    private String stckPrpr;       // 종가
    private String stckOprc;       // 시가
    private String stckHgpr;       // 고가
    private String stckLwpr;       // 저가
    private String cntgVol;        // 거래량
}

