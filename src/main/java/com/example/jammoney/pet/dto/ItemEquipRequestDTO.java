package com.example.jammoney.pet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEquipRequestDTO { //아이템 장착/해제 요청
    private Long itemId;
    private boolean equip; // true: 장착, false: 해제
}