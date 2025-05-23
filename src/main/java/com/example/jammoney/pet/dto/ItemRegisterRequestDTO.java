package com.example.jammoney.pet.dto;

import com.example.jammoney.pet.entity.ItemType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRegisterRequestDTO {
    private String name;
    private int price;
    private ItemType type;
    private String position;
    private String imageUrl;
    private String previewUrl;
}