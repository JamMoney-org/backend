package com.example.jammoney.stockApp.stock.entity;

import com.example.jammoney.stockApp.stock.entity.Enums.*;
import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    /** 주문 수량 */
    @Column(nullable = false)
    private int stockCount;

    /** 단가 (주문가 or 체결가) */
    @Column(nullable = false)
    private long price;

    /** 주문 상태 (WAITING / COMPLETED 등) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    /** 주문 타입 (BUY / SELL) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    /** 주문자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /** 종목 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private Company company;

    /** 체결 단가 */
    private Long executedPrice;

    /** 체결 시각 */
    private Instant executedAt;

    /** 마지막 수정 시각 */
    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    /** 동시 업데이트 방지 필드 */
    @Version
    private Long version;

    /* ============ 자동 시간 갱신 ============ */

    @PrePersist
    public void prePersist() {
        this.modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}
