package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
/**
 * 사용자가 보유한 주식 정보 응답 DTO
 * - 종목명, 보유 수량, 평가금액, 수익률, 자산 비중 포함
 * - 보유 주식 요약 카드 또는 포트폴리오 목록에 사용
 */

public class HoldingStockResponseDto {
    //보유 주식 id
    private long holdingStockId;

    //user의 id
    private long userId;

    //회사의 id
    private long companyId;

    //회사의 이름
    private String companyKorName;
    //보유 수량
    private int stockCount;

    //현재가
    private long currentPrice;
    //평가금
    private long evaluationAmount;
    //수익 금액 (평가금 - 매입금)


    //투자 금액
    private long totalPrice;

    private long profitAmount;
    //수익률 (수익금/매입금)*100
    private double profitRate;
    //전체 자산 대비 비중
    private double portfolioRatio;

    //예약 매도 수
    private int reserveSellStockCount;

    public HoldingStockResponseDto() {

    }
}
