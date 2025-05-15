package com.example.jammoney.pet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetStatusResponseDTO { //캐릭터 상태창 (레벨, 경험치, mood 등)
    private int level; //레벨
    private int exp; // 경험치
    private int nextLevelExp; // 다음 레벨까지 필요한 경험치 (경험치바 % 계산용)
    private String mood; //펫 상태
}