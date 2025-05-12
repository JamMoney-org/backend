package com.example.jammoney.pet.repository;

import com.example.jammoney.pet.entity.InventoryItem;
import com.example.jammoney.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.jammoney.pet.entity.Item;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByPet(Pet pet);

    Optional<InventoryItem> findByPetAndItem(Pet pet, Item item);

    List<InventoryItem> findByPetAndEquippedTrue(Pet pet);
}