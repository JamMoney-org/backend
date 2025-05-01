package com.example.Jammoney.StockApp.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_interest_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "stock_code"}))// 같은 사용자가 동일한 종목을 관심 종목으로 등록 못하도록 함
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterestStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stockCode;  // ex: "005930"

    @Column(nullable = false)
    private String stockName;  // ex: "삼성전자"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;
}
