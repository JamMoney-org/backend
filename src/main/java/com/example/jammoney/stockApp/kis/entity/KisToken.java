package com.example.jammoney.stockApp.kis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KisToken {

    @Id
    private Long id = 1L; // 단일 행만 저장

    @Column(nullable = false, length = 1024)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiredAt;
}
