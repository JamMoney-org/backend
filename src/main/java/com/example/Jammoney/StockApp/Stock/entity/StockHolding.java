package com.example.Jammoney.StockApp.Stock.entity;
import com.example.Jammoney.User.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    private int stockCount;

    private int reserveStockCount;

    private long price;
}

