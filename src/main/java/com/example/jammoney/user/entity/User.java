package com.example.jammoney.user.entity;
import com.example.jammoney.StockApp.stock.entity.Cash;
import com.example.jammoney.StockApp.stock.entity.InterestingStock;
import com.example.jammoney.StockApp.stock.entity.Order;
import com.example.jammoney.StockApp.stock.entity.HoldingStock;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(nullable = false)
    private boolean isActive;

    @OneToOne(mappedBy="user", cascade = CascadeType.ALL)
    private Cash cash;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterestingStock> interestingStocks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoldingStock> holdingStocks = new ArrayList<>();
}

