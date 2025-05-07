package com.example.jammoney.StockApp.stock.dto;

import com.example.jammoney.StockApp.stock.entity.Enums.OrderStatus;
import com.example.jammoney.StockApp.stock.entity.Enums.OrderType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderResponseDto {
    long stockOrderId;
    int stockCount;
    long userId;
    long companyId;
    OrderStatus orderStatus;
    OrderType orderType;
    long price;
}
