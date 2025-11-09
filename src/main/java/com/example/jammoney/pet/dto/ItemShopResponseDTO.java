package com.example.jammoney.pet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemShopResponseDTO { //상점에 보여줄 아이템 목록
    private Long itemId;
    private String name; //이름
    private String type;
    private long price; //가격
    private String previewUrl;   // 상점 미리보기 이미지
}