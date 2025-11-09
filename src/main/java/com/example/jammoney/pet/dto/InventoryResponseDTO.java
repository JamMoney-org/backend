package com.example.jammoney.pet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDTO { // 내가 가진 아이템 목록 + 장착 여부
    private Long itemId; //아이템 ID
    private String name; //아이템 이름
    private String type; //아이템 종류
    private boolean equipped; //장착 여부
    private String imageUrl; //이미지
    private String position; // 렌더링 위치 정보
}