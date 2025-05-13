package com.example.jammoney.pet.repository;

import com.example.jammoney.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
    // 사용자 기반으로 펫 조회 (양방향 매핑이 있다면 사용하지 않아도 됨)
    Pet findByUserId(Long userId);
}