package com.example.jammoney.auth.controller;

import com.example.jammoney.auth.dto.TokenRequestDto;
import com.example.jammoney.auth.dto.TokenResponseDto;
import com.example.jammoney.auth.entity.RefreshToken;
import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.service.RefreshTokenService;
import com.example.jammoney.exception.InvalidRefreshTokenException;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshAccessToken(@RequestBody TokenRequestDto request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);  // 토큰이 email 정보 포함하고 있다고 가정

        if (!refreshTokenService.validate(email, refreshToken)) {
            throw new InvalidRefreshTokenException("RefreshToken 불일치 또는 없음");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = refreshTokenService.createRefreshToken(email);  // Sliding Expiration

        return ResponseEntity.ok(new TokenResponseDto(newAccessToken, newRefreshToken));
    }
}