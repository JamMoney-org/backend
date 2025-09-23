package com.example.jammoney.auth.controller;

import com.example.jammoney.auth.dto.TokenRequestDto;
import com.example.jammoney.auth.dto.TokenResponseDto;
import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.service.RefreshTokenService;
import com.example.jammoney.exception.UserNotFoundException;
import com.example.jammoney.user.dto.LoginRequestDto;
import com.example.jammoney.user.dto.UserRequestDto;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import com.example.jammoney.user.service.UserService;
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
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserRequestDto request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 로그인 (Access / Refresh 발급)
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto request) {
        // 1. Security를 통한 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. User 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        // 3. JWT 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 4. Refresh Token 저장 (Redis) — 여러 기기 허용 시 Set 구조 사용
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        return ResponseEntity.ok(new TokenResponseDto(accessToken, refreshToken));
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> reissue(@RequestBody TokenRequestDto request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().build();
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Redis에 저장된 refreshToken과 일치하는지 확인
        refreshTokenService.assertTokenValid(userId, refreshToken);

        // 새로운 토큰 발급
        String newAccess = jwtTokenProvider.generateAccessToken(userId);
        String newRefresh = refreshTokenService.reissueRefreshToken(userId, refreshToken);

        return ResponseEntity.ok(new TokenResponseDto(newAccess, newRefresh));
    }

    /**
     * 로그아웃 (단일 기기)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRequestDto request) {
        String accessToken = request.getAccessToken();
        String refreshToken = request.getRefreshToken();

        // AccessToken 블랙리스트 등록
        refreshTokenService.blacklistAccessToken(accessToken);

        // RefreshToken 제거
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        refreshTokenService.invalidateOne(userId, refreshToken);

        return ResponseEntity.ok().build();
    }

    /**
     * 전체 로그아웃 (모든 기기)
     */
    @PostMapping("/logout/all/{userId}")
    public ResponseEntity<Void> logoutAll(@PathVariable Long userId) {
        refreshTokenService.invalidateAll(userId);
        return ResponseEntity.ok().build();
    }
}
