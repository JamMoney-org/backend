package com.example.jammoney.stockApp.stock.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    //시가총액 (단위: 원)
    private Long marketCap;

    //액면가 (단위: 원)
    private Long faceValue;

    // 상장일자
    private LocalDate listedDate;

    // 상장 주식 수
    private Long listedShares;

    // 업종 분류
    private String industry;

    // 배당수익률 (단위: %)
    private BigDecimal dividendYield;

    // 주당배당금 (단위: 원)
    private BigDecimal dividendPerShare;

    // EPS (주당순이익)
    private BigDecimal eps;

    // PER (주가수익비율)
    private BigDecimal per;

    // BPS (주당순자산가치)
    private BigDecimal bps;

    // PBR (주가순자산비율)
    private BigDecimal pbr;

    // 결산월
    private String settlementMonth;
}
