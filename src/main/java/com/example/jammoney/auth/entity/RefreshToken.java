package com.example.jammoney.auth.entity;

import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @OneToOne(fetch = FetchType.EAGER)//리프레시 토큰 갱신/검증 때마다 프록시 로딩이 발생하는 현상 방지
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isNearExpiry(long thresholdInDays) {
        return expiryDate.isBefore(LocalDateTime.now().plusDays(thresholdInDays));
    }
}