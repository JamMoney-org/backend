package com.example.jammoney.user.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.user.dto.UserProfileDto;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.toUser();  // CustomUserDetails 내부에 toUser() 메서드 있어야 함
        return ResponseEntity.ok(new UserProfileDto(user));
    }

    //닉네임 변경
    @PatchMapping("/nickname")
    public ResponseEntity<String> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String newNickname
    ) {
        userService.updateNickname(userDetails.getUsername(), newNickname);  // 이메일 기준
        return ResponseEntity.ok("닉네임 변경 완료");
    }

    //회원탈퇴
    @DeleteMapping
    public ResponseEntity<String> deactivateUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deactivate(userDetails.getUsername());  // 이메일 기준
        return ResponseEntity.ok("회원 탈퇴 완료");
    }
}