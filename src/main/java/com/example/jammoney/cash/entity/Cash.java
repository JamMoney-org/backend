package com.example.jammoney.cash.entity;
import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cash{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cashId;

    @Column(nullable = false)
    private long money;

    @JoinColumn(name = "USER_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    public void increase(long amount) {
        if (amount < 0) throw new IllegalArgumentException("증가 금액은 0 이상이어야 합니다.");
        this.money += amount;
    }

    public void decrease(long amount) {
        if (amount < 0) throw new IllegalArgumentException("감소 금액은 0 이상이어야 합니다.");
        if (this.money < amount) throw new IllegalStateException("잔액이 부족합니다.");
        this.money -= amount;
    }

}
