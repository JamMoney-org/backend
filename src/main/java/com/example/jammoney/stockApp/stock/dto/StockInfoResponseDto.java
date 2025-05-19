package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * 종목의 실시간 시세 정보 응답 DTO
 * - 현재가, 전일 대비, 거래량/거래대금 포함
 * - 종목 리스트/상세화면, 관심종목 등에 사용
 */

public class StockInfoResponseDto {
    private long stockInfoId;

    private long companyId;
    //주식 현재가
    private String stck_prpr;
    //전일 대비
    private String prdy_vrss;
    //전일 대비율
    private String prdy_ctrt;
    //누적 거래량
    private String acml_vol;
    //누적 거래대금
    private String acml_tr_pbmn;
}
