package com.example.Jammoney.StockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HoldingStockResponseDto {
    private long stockHoldId;
    private long memberId;
    private long companyId;
    private String companyKorName;
    private int stockCount;
    private long totalPrice;
    private double percentage;
    private long stockReturn;
    private long reserveSellStockCount;
}
