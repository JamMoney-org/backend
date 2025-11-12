package com.example.jammoney.stockApp.stock.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
/**
 * 특정 종목의 1분봉 등 단기 체결 시세 응답 DTO
 * - 시간별 시가, 고가, 저가, 종가, 거래량 포함
 * - 분봉 차트 데이터 구성에 사용
 */

public class StockMinResponseDto {
    //분봉 정보 id
    private long stockMinId;

    //회사 id
    private long companyId;

    //stck_cntg_hour이 LocalDateTime으로 변환
    private LocalDateTime stockTradeTime;

    //주식 체결 시간(문자열)
    private String stck_cntg_hour;

    //주식 종가
    private String stck_prpr;

    //주식 시가
    private String stck_oprc;

    //주식 고가
    private String stck_hgpr;

    //주식 저가
    private String stck_lwpr;

    //체결 거래량
    private String cntg_vol;
}

