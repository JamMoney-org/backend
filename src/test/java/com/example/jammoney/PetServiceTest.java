package com.example.jammoney;

import com.example.jammoney.User.User;
import com.example.jammoney.pet.dto.PetStatusResponseDTO;
import com.example.jammoney.pet.entity.Pet;
import com.example.jammoney.pet.repository.PetRepository;
import com.example.jammoney.pet.service.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @InjectMocks
    private PetService petService;

    @Mock
    private PetRepository petRepository;

    // ------------------- renamePet -------------------
    @Test
    void 이름_정상_변경() {
        User user = User.builder().id(1L).build();
        Pet pet = Pet.builder().name("공룡이").build();
        Mockito.when(petRepository.findByUserId(1L)).thenReturn(pet);

        petService.renamePet(user, "마루");

        assertEquals("마루", pet.getName());
        Mockito.verify(petRepository).save(pet);
    }

    @Test
    void 이름_null_예외() {
        User user = User.builder().id(1L).build();
        Pet pet = Pet.builder().build();
        Mockito.when(petRepository.findByUserId(1L)).thenReturn(pet);

        assertThrows(IllegalArgumentException.class, () -> {
            petService.renamePet(user, null);
        });
    }

    @Test
    void 이름_공백_예외() {
        User user = User.builder().id(1L).build();
        Pet pet = Pet.builder().build();
        Mockito.when(petRepository.findByUserId(1L)).thenReturn(pet);

        assertThrows(IllegalArgumentException.class, () -> {
            petService.renamePet(user, "   ");
        });
    }

    @Test
    void 이름_길이초과_예외() {
        User user = User.builder().id(1L).build();
        Pet pet = Pet.builder().build();
        Mockito.when(petRepository.findByUserId(1L)).thenReturn(pet);

        String longName = "이름이이이이이이이이이이이이이이이이이이이이";
        assertThrows(IllegalArgumentException.class, () -> {
            petService.renamePet(user, longName);
        });
    }

    // ------------------- getPetStatus -------------------
    @Test
    void 캐릭터_상태_정상조회() {
        Pet pet = Pet.builder().level(3).exp(67).mood("Happy").build();
        User user = User.builder().pet(pet).build();

        PetStatusResponseDTO status = petService.getPetStatus(user);

        assertEquals(3, status.getLevel());
        assertEquals(67, status.getExp());
        assertEquals(100, status.getNextLevelExp());
        assertEquals("Happy", status.getMood());
    }

    // ------------------- addExp -------------------
    @Test
    void 경험치_추가_및_레벨업() {
        Pet pet = Pet.builder().level(1).exp(0).build();
        User user = User.builder().pet(pet).build();

        petService.addExp(user, 60); // 50 이상이면 1 → 2 레벨업

        assertEquals(2, pet.getLevel());
        assertEquals(10, pet.getExp()); // 60 - 50
    }

    @Test
    void 경험치_만렙_이상은_변화없음() {
        Pet pet = Pet.builder().level(10).exp(0).build();
        User user = User.builder().pet(pet).build();

        petService.addExp(user, 1000);

        assertEquals(10, pet.getLevel());
        assertEquals(0, pet.getExp());
    }

    // ------------------- getPetImageName -------------------
    @Test
    void 이미지_파일명_정상생성() {
        assertEquals("pet_level_3.png", petService.getPetImageName(3));
        assertEquals("pet_max.png", petService.getPetImageName(10));
    }

    // ------------------- getExpPercentage -------------------
    @Test
    void 경험치퍼센트_정상계산() {
        Pet pet = Pet.builder().level(2).exp(25).build(); // 2 → 3 필요 경험치: 75
        int percent = petService.getExpPercentage(pet);

        assertEquals(33, percent); // 약 33%
    }

    @Test
    void 만렙이면_퍼센트_100() {
        Pet pet = Pet.builder().level(10).exp(0).build();
        int percent = petService.getExpPercentage(pet);

        assertEquals(100, percent);
    }

    // ------------------- isMaxLevel -------------------
    @Test
    void 만렙확인() {
        Pet pet = Pet.builder().level(10).build();
        assertTrue(petService.isMaxLevel(pet));

        Pet notMax = Pet.builder().level(7).build();
        assertFalse(petService.isMaxLevel(notMax));
    }
}