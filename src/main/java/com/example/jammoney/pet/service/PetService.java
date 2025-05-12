package com.example.jammoney.pet.service;
import com.example.jammoney.pet.dto.PetStatusResponseDTO;
import com.example.jammoney.pet.entity.Pet;
import com.example.jammoney.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;

    public void renamePet(User user, String newName) { //이름 설정
        Pet pet = petRepository.findByUserId(user.getId());

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }

        if (newName.length() > 20) {
            throw new IllegalArgumentException("이름은 20자 이내여야 합니다.");
        }

        pet.setName(newName.trim());
        petRepository.save(pet);
    }

    /**
     * 캐릭터 상태 조회
     */
    public PetStatusResponseDTO getPetStatus(User user) {
        Pet pet = user.getPet();
        int currentLevel = pet.getLevel();
        int currentExp = pet.getExp();
        int nextLevelExp = getRequiredExpForLevel(currentLevel + 1);
        String mood = pet.getMood();

        return PetStatusResponseDTO.builder()
                .level(currentLevel)
                .exp(currentExp)
                .nextLevelExp(nextLevelExp) // 확장: 경험치바 계산용
                .mood(mood)
                .build();
    }

    /**
     * 경험치 추가 및 레벨업 처리
     */
    public void addExp(User user, int gainedExp) {
        Pet pet = user.getPet();
        if (isMaxLevel(pet)) return; // 이미 만렙이면 무시

        int newExp = pet.getExp() + gainedExp;
        int level = pet.getLevel();

        while (newExp >= getRequiredExpForLevel(level + 1)) {
            newExp -= getRequiredExpForLevel(level + 1);
            level++;
            if (level >= 10) {
                level = 10;
                newExp = 0;
                break;
            }
        }

        pet.setExp(newExp);
        pet.setLevel(level);
        pet.setMood(calculateMood(pet));

        petRepository.save(pet);
    }

    /**
     * 레벨별 필요 경험치 계산
     * 예: 1→2는 50, 2→3은 75, ... 10레벨은 만렙
     */
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
            default -> Integer.MAX_VALUE; // 만렙 이후 경험치 무의미
        };
    }

    // 공룡 이미지 이름 반환 (레벨별 이미지 관리)
    public String getPetImageName(int level) {
        if (level >= 10) return "pet_max.png";
        return "pet_level_" + level + ".png"; // 예: pet_level_3.png
    }

    //경험치 퍼센트 계산 (경험치바용)
    public int getExpPercentage(Pet pet) {
        int level = pet.getLevel();
        int currentExp = pet.getExp();
        int nextExp = getRequiredExpForLevel(level + 1);

        if (nextExp == Integer.MAX_VALUE) return 100; // 만렙
        return (int) ((double) currentExp / nextExp * 100);
    }

    //만렙 여부 체크
    public boolean isMaxLevel(Pet pet) {
        return pet.getLevel() >= 10;
    }

    //mood 상태 자동 변화
    private String calculateMood(Pet pet) {
        if (isMaxLevel(pet)) return "Proud";
        if (pet.getExp() == 0) return "Sleepy";
        return "Happy";
    }
}