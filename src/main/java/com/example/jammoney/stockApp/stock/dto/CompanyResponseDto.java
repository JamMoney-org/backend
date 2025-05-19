package com.example.jammoney.stockApp.stock.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * 종목 상세 정보 응답 DTO (복합 구조)
 * - companyId, code, korName: 기본 종목 정보
 * - stockAskingPriceResponseDto: 10단계 호가 정보
 * - stockInfResponseDto: 현재가/전일 대비/거래량 등 시세 정보
 * - 종목 상세 조회 페이지 구성에 사용
 */

public class CompanyResponseDto {
    private long companyId;
    private String code;
    private String korName;
    private StockAskingPriceResponseDto stockAskingPriceResponseDto; // 10단계 호가
    private StockInfoResponseDto stockInfoResponseDto;   // 현재가 등 시세 정보
}
