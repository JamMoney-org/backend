package com.example.jammoney.pet;

import jakarta.persistence.*;

@Entity
public class Pet {
    @Id @GeneratedValue
    private Long id;

    //@OneToOne
    //private User user;

    private int level;              // 현재 레벨 (1~10)
    private int exp;                // 현재 경험치

    private String mood;           // 예: Happy, Hungry, Sleepy
}