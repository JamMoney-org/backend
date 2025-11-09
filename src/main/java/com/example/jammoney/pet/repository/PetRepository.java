package com.example.jammoney.pet.repository;

import com.example.jammoney.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
    Pet findByUserId(Long userId);
}