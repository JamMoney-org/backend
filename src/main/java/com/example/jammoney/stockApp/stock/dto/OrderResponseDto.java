package com.example.jammoney.stockApp.stock.dto;

import com.example.jammoney.stockApp.stock.entity.Enums.OrderStatus;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
/**
 * 주문 결과 응답 DTO
 * - 주문 ID, 수량, 가격, 상태, 생성 시간 포함
 * - 주문 내역 조회 또는 주문 완료 알림에 사용
 */

public class OrderResponseDto {
    long orderId;
    int stockCount;
    long userId;
    long companyId;
    OrderStatus orderStatus;
    OrderType orderType;
    long price;
    private LocalDateTime createdAt;
}
