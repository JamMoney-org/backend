package com.example.jammoney.stockApp.stock.event;

import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderChangedEvent {
    private final Long userId;
    private final List<OrderResponseDto> buyOrders;
    private final List<OrderResponseDto> sellOrders;

    public OrderChangedEvent(Long userId, List<OrderResponseDto> buyOrders, List<OrderResponseDto> sellOrders) {
        this.userId = userId;
        this.buyOrders = buyOrders;
        this.sellOrders = sellOrders;
    }
}
