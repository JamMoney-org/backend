package com.example.jammoney.pet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id @GeneratedValue
    private Long id;

    private String name;                        // 아이템 이름

    @Enumerated(EnumType.STRING)
    private ItemType type;                      // HAT, BACKGROUND, ACCESSORY 등

    private long price;                          // 가상 머니 가격
    private String imageUrl;                    // 착용 이미지 (투명 PNG 등)
    private String previewUrl;                  // 상점용 미리보기 썸네일
    private String position;
}