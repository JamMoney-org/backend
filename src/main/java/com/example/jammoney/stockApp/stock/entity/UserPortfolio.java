package com.example.jammoney.stockApp.stock.entity;

import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // 보유 주식 자산 총액
    private long stockAsset;

    // 총 평가 자산 = 현금 + 주식
    private long totalAsset;

    // 수익금 = 총 평가 자산 - 투자원금
    private long profitAmount;

    // 수익률 (%)
    private double profitRate;
}
