package com.example.jammoney.pet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetRenameRequestDTO {
    private String newName;
}