package com.example.jammoney.auth.controller;

import com.example.jammoney.auth.dto.TokenRequestDto;
import com.example.jammoney.auth.dto.TokenResponseDto;
import com.example.jammoney.auth.entity.RefreshToken;
import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.service.RefreshTokenService;
import com.example.jammoney.exception.ErrorCode;
import com.example.jammoney.exception.InvalidRefreshTokenException;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshAccessToken(@RequestBody TokenRequestDto requestDto) {
        String refreshTokenValue = requestDto.getRefreshToken();
        log.info("/auth/refresh 요청 진입");
        log.info("요청된 refreshToken: {}", refreshTokenValue);

        // 1. 토큰 형식 유효성 검사 (서명, 만료 여부 포함)
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new InvalidRefreshTokenException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. DB 조회 (회수된 토큰이나 위조된 토큰 차단 목적)
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .filter(rt -> !rt.isExpired())
                .orElseThrow(() -> new InvalidRefreshTokenException(ErrorCode.INVALID_REFRESH_TOKEN));

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String newRefreshToken = refreshToken.getToken();

        // 3. 만료 임박 시 refreshToken 교체 및 기존 삭제
        if (refreshToken.isNearExpiry(3)) {
            refreshTokenService.deleteByEmail(user.getEmail());
            RefreshToken updated = refreshTokenService.createRefreshToken(user);
            newRefreshToken = updated.getToken();
        }

        return ResponseEntity.ok(new TokenResponseDto(newAccessToken, newRefreshToken));
    }
}