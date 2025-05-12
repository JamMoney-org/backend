package com.example.jammoney.user.entity;
import com.example.jammoney.stockApp.stock.entity.*;
import com.example.jammoney.user.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

    // 관계 매핑 생략

    @Builder
    public User(String email, String password, String nickname, Role role, boolean active) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.isActive = active;
    }

    // 테스트용 생성자 추가
    public User(Long id, String email, String password, String nickname, Role role, boolean active) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.isActive = active;
    }
}