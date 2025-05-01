package com.example.Jammoney.StockApp.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "stock_holding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockHoldingId;

    @Column(nullable = false)
    private String stockName;

    @Column(nullable = false)
    private String stockCode;

    private Long profit; // 총 수익

    private Double profitRate; // 수익률

    private int amount; // 보유 수량

    private Long avgBuying; // 평균 매입가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

