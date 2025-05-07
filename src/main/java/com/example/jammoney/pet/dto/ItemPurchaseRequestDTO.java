package com.example.jammoney.pet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPurchaseRequestDTO { //아이템 구매 요청
    private Long itemId; //이거 구매할래!
}