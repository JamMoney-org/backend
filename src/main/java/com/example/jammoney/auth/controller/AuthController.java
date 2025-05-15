package com.example.jammoney.auth.controller;

import com.example.jammoney.auth.dto.TokenResponseDto;
import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.service.RedisService;
import com.example.jammoney.auth.service.RefreshTokenService;
import com.example.jammoney.user.dto.LoginRequestDto;
import com.example.jammoney.user.dto.UserRequestDto;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final RedisService redisService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {

        // 1. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
        );

        // 2. 인증된 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 3. AccessToken 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // 4. RefreshToken 생성 및 저장

        // 5. 클라이언트에 응답
        return ResponseEntity.ok(new TokenResponseDto(accessToken, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        String accessToken = bearerToken.replace("Bearer ", "");
        String email = jwtTokenProvider.getEmailFromToken(accessToken);

        long remainingTime = jwtTokenProvider.getRemainingTime(accessToken);
        redisService.blacklistAccessToken(accessToken, remainingTime);
        refreshTokenService.deleteByEmail(email);

        return ResponseEntity.ok("로그아웃 완료");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입 성공!");
    }
}
