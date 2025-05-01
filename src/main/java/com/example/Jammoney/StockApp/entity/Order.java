package com.example.Jammoney.StockApp.entity;

import com.example.Jammoney.StockApp.entity.Enums.OrderCategory;
import com.example.Jammoney.StockApp.entity.Enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderCategory orderCategory; // BUY / SELL

    private LocalDateTime transactionDate; // 체결 일시

    @Column(nullable = false)
    private int transactionAmount;

    private Long transactionPrice; // 체결가 (예약 주문이면 null)

    @Column(nullable = false)
    private boolean isReserved;

    private Long reservedPrice; // 희망가 (예약일 경우)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // PENDING / EXECUTED / CANCELED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String stockName; // 종목명 (정규화 안할 경우 그대로 사용)
}

