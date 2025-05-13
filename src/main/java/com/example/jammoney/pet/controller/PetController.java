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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final UserRepository userRepository;

    // 현재 로그인한 유저를 DB에서 다시 가져오는 유틸 메서드
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    // 캐릭터 상태 조회
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<PetStatusResponseDTO>> getPetStatus() {
        User user = getCurrentUser();
        PetStatusResponseDTO status = petService.getPetStatus(user);
        return ResponseEntity.ok(ApiResponse.success("캐릭터 상태 조회 성공", status));
    }

    // 캐릭터 이름 변경
    @PostMapping("/rename")
    public ResponseEntity<ApiResponse<Void>> renamePet(@RequestBody PetRenameRequestDTO request) {
        User user = getCurrentUser();
        petService.renamePet(user, request.getNewName());
        return ResponseEntity.ok(ApiResponse.success("캐릭터 이름 변경 완료", null));
    }
}