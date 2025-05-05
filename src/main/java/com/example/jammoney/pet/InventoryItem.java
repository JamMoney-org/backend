package com.example.jammoney.pet;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class InventoryItem {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Pet pet;

    @ManyToOne
    private Item item;

    private boolean equipped;
}