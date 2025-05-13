package com.example.jammoney.stockApp.stock.dto;

import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    @NotBlank(message = "종목 코드는 필수입니다.")
    private String companyCode;

    @Min(value = 1, message = "1주 이상 주문해야 합니다.")
    private int stockCount;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private long price; // 시장가일 경우 무시

    @NotNull
    private OrderType orderType;     // BUY / SELL

    @NotBlank
    private String tradeType;        // MARKET / LIMIT
}
