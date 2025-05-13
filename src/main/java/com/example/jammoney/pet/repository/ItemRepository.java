package com.example.jammoney.pet.repository;

import com.example.jammoney.pet.entity.Item;
import com.example.jammoney.pet.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    // 상점에 보여줄 모든 아이템
    List<Item> findByType(ItemType type);
}