package com.example.jammoney.cash.entity;

import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "cash")
public class Cash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 실제 가용 현금 (예약 제외) */
    @Column(nullable = false)
    private long money;

    /** 예약된 현금 (BUY 예약 주문용) */
    @Column(nullable = false)
    private long reservedCash;

    /** 낙관적 락 */
    @Version
    private Long version;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /* ====== 유틸 메서드 ====== */

    public long available() {
        return money - reservedCash;
    }

    public void increase(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        this.money += amount;
    }

    public void decrease(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        if (this.money < amount) throw new IllegalStateException("not enough money");
        this.money -= amount;
    }

    public void reserve(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        if (available() < amount) throw new IllegalStateException("not enough available money to reserve");
        this.reservedCash += amount;
    }

    public void releaseReserved(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        if (this.reservedCash < amount) throw new IllegalStateException("reservedCash underflow");
        this.reservedCash -= amount;
    }

    /** 예약금 → 실차감: reservedCash 감소 + money 감소 */
    public void commitReserved(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        if (this.reservedCash < amount) throw new IllegalStateException("reservedCash underflow");
        this.reservedCash -= amount;
        if (this.money < amount) throw new IllegalStateException("money underflow during commit");
        this.money -= amount;
    }
}
