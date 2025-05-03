package com.example.Jammoney.User;
import com.example.Jammoney.StockApp.Stock.entity.Cash;
import com.example.Jammoney.StockApp.Stock.entity.InterestedStock;
import com.example.Jammoney.StockApp.Stock.entity.Order;
import com.example.Jammoney.StockApp.Stock.entity.StockHolding;
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
    private List<InterestedStock> interestedStocks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockHolding> stockHoldings = new ArrayList<>();
}

