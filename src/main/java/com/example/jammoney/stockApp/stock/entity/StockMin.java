package com.example.jammoney.stockApp.stock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_min",
        uniqueConstraints = @UniqueConstraint(columnNames = {"COMPANY_ID", "stock_trade_time"})
)

@Getter
@Setter
@NoArgsConstructor
public class StockMin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockMinId;

    @ManyToOne
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private Company company;

    @Column(nullable = false)
    private LocalDateTime stockTradeTime;

    // 주식 체결 시간(문자열)
    private String stck_cntg_hour;

    // 주식 종가
    private String stck_prpr;

    // 주식 시가
    private String stck_oprc;

    // 주식 고가
    private String stck_hgpr;

    // 주식 저가
    private String stck_lwpr;

    // 체결 거래량
    private String cntg_vol;

    public void setTradeTime(LocalDateTime now) {
        int hour = Integer.parseInt(this.stck_cntg_hour.substring(0, 2));
        int minute = Integer.parseInt(this.stck_cntg_hour.substring(2, 4));
        int second = Integer.parseInt(this.stck_cntg_hour.substring(4, 6));

        this.stockTradeTime = LocalDateTime.of(
                now.getYear(),
                now.getMonth(),
                now.getDayOfMonth(),
                hour,
                minute,
                second
        );
    }
}


