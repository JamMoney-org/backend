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
    //주문 id
    private long orderId;

    //주문 수량
    private int stockCount;

    //user id
    private long userId;

    //회사 id
    private long companyId;

    //주문 상태 (COMPLETED, WAITING)
    private OrderStatus orderStatus;

    //주문 타입 (BUY, SELL)
    private OrderType orderType;

    //가격
    private long price;

    //주문 요청 시간
    private LocalDateTime createdAt;
}
