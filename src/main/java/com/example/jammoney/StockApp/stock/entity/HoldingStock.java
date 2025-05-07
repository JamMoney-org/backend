package com.example.jammoney.StockApp.stock.entity;
import com.example.jammoney.User.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "holding_stock")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HoldingStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdingStockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    //보유 수량
    private int stockCount;

    //총 매입 금액
    private long totalPrice;
}

