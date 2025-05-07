package com.example.jammoney.pet;

import jakarta.persistence.*;

@Entity
public class InventoryItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    private boolean equipped; // 현재 장착 여부
}