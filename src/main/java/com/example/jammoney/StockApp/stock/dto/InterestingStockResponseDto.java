package com.example.jammoney.StockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InterestingStockResponseDto {
    private long interestingStockId;
    private String companyName;
    private String currentPrice;
    private String priceChange;
    private String priceChangeRate;
    private String volume;
    private String tradeValue;
}
