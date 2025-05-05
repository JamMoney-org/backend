package com.example.jammoney.pet;

import com.example.jammoney.pet.GrowthStage;
import jakarta.persistence.*;

@Entity
public class Pet {
    @Id @GeneratedValue
    private Long id;

    //@OneToOne
    //private User user;

    private int level;              // 현재 레벨 (1~10)
    private int exp;                // 현재 경험치
    private int money;             // 가상 머니

    private String mood;           // 예: Happy, Hungry, Sleepy
}