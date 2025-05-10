package com.example.jammoney.stockApp.stock.entity;

import com.example.jammoney.stockApp.stock.entity.Enums.*;
import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Order{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stockOrderId;

    @Column
    private int stockCount;

    @ManyToOne()
    @JoinColumn(name = "MEMBER_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    @ManyToOne()
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    @Enumerated(EnumType.STRING) // 항상 명시하자!
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING) // 항상 명시하자!
    private OrderType orderType;

    private long price;

}
