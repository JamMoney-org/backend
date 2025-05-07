package com.example.Jammoney.StockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
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

