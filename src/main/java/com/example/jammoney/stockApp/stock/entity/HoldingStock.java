package com.example.jammoney.stockApp.stock.entity;
import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "holding_stock")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class HoldingStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdingStockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    //보유 수량
    private int stockCount;

    //예약 주문 수량
    private int reserveStockCount;
    //총 매입 금액
    private long totalPrice;

    public HoldingStock(User user, Company company, int stockCount, int totalPrice) {
        this.user = user;
        this.company = company;
        this.stockCount = stockCount;
        this.totalPrice = totalPrice;
    }
}

