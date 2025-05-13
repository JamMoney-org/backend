package com.example.jammoney.pet.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.exception.ApiResponse;
import com.example.jammoney.pet.dto.PetRenameRequestDTO;
import com.example.jammoney.pet.dto.PetStatusResponseDTO;
import com.example.jammoney.pet.service.PetService;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    // 캐릭터 상태 조회
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<PetStatusResponseDTO>> getPetStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        PetStatusResponseDTO status = petService.getPetStatus(user);
        return ResponseEntity.ok(ApiResponse.success("캐릭터 상태 조회 성공", status));
    }

    // 캐릭터 이름 변경
    @PostMapping("/rename")
    public ResponseEntity<ApiResponse<Void>> renamePet(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PetRenameRequestDTO request) {

        User user = userDetails.getUser();
        petService.renamePet(user, request.getNewName());
        return ResponseEntity.ok(ApiResponse.success("캐릭터 이름 변경 완료", null));
    }
}