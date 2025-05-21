package com.example.jammoney.stockApp.stock.dto;

import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import com.example.jammoney.stockApp.stock.entity.Enums.TradeType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
/**
 * 주식 주문 요청 DTO
 * - 종목 코드, 수량, 가격, 주문/거래 유형을 포함
 * - 지정가/시장가, 매수/매도 구분
 * - 주문 생성 시 클라이언트 → 서버로 전달되는 입력값
 */

public class OrderRequestDto {

    //회사 코드
    @NotBlank(message = "종목 코드는 필수입니다.")
    private String companyCode;

    //주문 수량
    @Min(value = 1, message = "1주 이상 주문해야 합니다.")
    private int stockCount;

    //가격 (시장가일 경우 무시)
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private long price;

    //주문 종류 (BUY/SELL)
    @NotNull
    private OrderType orderType;

    //거래 종류 (MARKET[시장가]/LIMIT[지정가])
    @NotNull
    private TradeType tradeType;
}
