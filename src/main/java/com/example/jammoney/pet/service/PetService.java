package com.example.jammoney.pet.service;

import com.example.jammoney.pet.dto.PetStatusResponseDTO;
import com.example.jammoney.pet.entity.Pet;
import com.example.jammoney.pet.repository.PetRepository;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public void renamePet(User user, String newName) {
        Pet pet = user.getPet();

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }

        if (newName.length() > 20) {
            throw new IllegalArgumentException("이름은 20자 이내여야 합니다.");
        }

        pet.setName(newName.trim());
        petRepository.save(pet);
    }

    public PetStatusResponseDTO getPetStatus(User user) {
        Pet pet = user.getPet();

        String currentName = pet.getName();
        int currentLevel = pet.getLevel();
        int currentExp = pet.getExp();
        int nextLevelExp = getRequiredExpForLevel(currentLevel + 1);
        int expPercentage = getExpPercentage(pet);
        String mood = calculateMood(currentLevel, currentExp); // ✅ 인자 기반 계산

        return PetStatusResponseDTO.builder()
                .name(currentName)
                .level(currentLevel)
                .exp(currentExp)
                .nextLevelExp(nextLevelExp)
                .mood(mood)
                .expPercentage(expPercentage)
                .build();
    }

    public void addExp(User user, int gainedExp) {
        Pet pet = user.getPet();
        if (isMaxLevel(pet)) return;

        int currentExp = pet.getExp() + gainedExp;
        int currentLevel = pet.getLevel();

        while (currentExp >= getRequiredExpForLevel(currentLevel + 1)) {
            currentExp -= getRequiredExpForLevel(currentLevel + 1);
            currentLevel++;
            if (currentLevel >= 10) {
                currentLevel = 10;
                currentExp = 0;
                break;
            }
        }

        pet.setLevel(currentLevel);
        pet.setExp(currentExp);
        pet.setMood(calculateMood(currentLevel, currentExp));

        petRepository.save(pet);
    }

    private int getRequiredExpForLevel(int level) {
        return switch (level) {
            case 2 -> 50;
            case 3 -> 75;
            case 4 -> 100;
            case 5 -> 150;
            case 6 -> 200;
            case 7 -> 250;
            case 8 -> 300;
            case 9 -> 400;
            case 10 -> 500;
            default -> Integer.MAX_VALUE;
        };
    }

    public String getPetImageName(int level) {
        if (level >= 10) return "pet_max.png";
        return "pet_level_" + level + ".png";
    }

    public int getExpPercentage(Pet pet) {
        int level = pet.getLevel();
        int currentExp = pet.getExp();
        int nextExp = getRequiredExpForLevel(level + 1);

        if (nextExp == Integer.MAX_VALUE) return 100;
        return (int) ((double) currentExp / nextExp * 100);
    }

    public boolean isMaxLevel(Pet pet) {
        return pet.getLevel() >= 10;
    }

    private String calculateMood(int level, int exp) {
        if (level >= 10) return "Proud";
        if (exp == 0) return "Sleepy";
        return "Happy";
    }
}