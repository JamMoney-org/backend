package com.example.jammoney.user.entity;

import com.example.jammoney.pet.entity.Pet;
import com.example.jammoney.stockApp.stock.entity.Cash;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.stockApp.stock.entity.InterestingStock;
import com.example.jammoney.stockApp.stock.entity.Order;
import com.example.jammoney.user.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user")
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

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cash cash;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Pet pet;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterestingStock> interestingStocks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoldingStock> holdingStocks = new ArrayList<>();

    public User(Long id, String email, String password, String nickname, Role role, boolean active) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.isActive = active;
    }
}
