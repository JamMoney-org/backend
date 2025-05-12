package com.example.jammoney.pet.entity;

import com.example.jammoney.User.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Pet {
    @Id @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 20)
    private String name;  // 캐릭터 이름 (예: “공룡이”, “나의친구”)
    private int level;              // 현재 레벨 (1~10)
    private int exp;                // 현재 경험치

    private String mood;           // 예: Happy, Hungry, Sleepy
}