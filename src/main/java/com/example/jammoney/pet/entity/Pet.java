package com.example.jammoney.pet.entity;

import com.example.jammoney.user.entity.User;
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
    private String name;
    private int level;
    private int exp;

    private String mood;
}