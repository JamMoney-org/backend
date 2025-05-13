package com.example.jammoney.auth.controller;

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
    public ResponseEntity<TokenResponseDto> refreshAccessToken(@RequestParam("refreshToken") String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .filter(rt -> !rt.isExpired())
                .orElseThrow(() -> new InvalidRefreshTokenException("리프레시 토큰이 만료되었거나 유효하지 않습니다."));


        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String newRefreshToken = refreshToken.getToken();

        // Sliding 만료: 3일 이하 남았으면 새 RefreshToken도 발급
        if (refreshToken.isNearExpiry(3)) {
            RefreshToken updated = refreshTokenService.createRefreshToken(user);
            newRefreshToken = updated.getToken();
        }

        return ResponseEntity.ok(new TokenResponseDto(newAccessToken, newRefreshToken));
    }
}