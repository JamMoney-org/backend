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
    //회사 id
    private long companyId;

    //회사 코드
    private String code;

    //회사 이름
    private String korName;

    //회사의 10단계 호가
    private StockAskingPriceResponseDto stockAskingPriceResponseDto;

    //회사의 시세 정보
    private StockInfoResponseDto stockInfoResponseDto;
}
