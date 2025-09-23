package com.example.jammoney.auth.service;

import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.repository.RefreshTokenRepository;
import com.example.jammoney.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    // Refresh Token 저장
    public void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenRepository.saveByUserId(userId, refreshToken, REFRESH_TOKEN_TTL);
    }

    // Refresh Token 유효성 검증
    public void assertTokenValid(Long userId, String providedToken) {
        if (!jwtTokenProvider.validateToken(providedToken)) {
            throw new InvalidRefreshTokenException();
        }
        if (!refreshTokenRepository.existsByUserIdAndToken(userId, providedToken)) {
            throw new InvalidRefreshTokenException();
        }
    }

    // Refresh Token 재발급
    public String reissueRefreshToken(Long userId, String oldToken) {
        assertTokenValid(userId, oldToken);
        refreshTokenRepository.deleteByUserIdAndToken(userId, oldToken);
        String newToken = jwtTokenProvider.generateRefreshToken(userId);
        saveRefreshToken(userId, newToken);
        return newToken;
    }

    // 단일 로그아웃 (특정 기기)
    public void invalidateOne(Long userId, String providedToken) {
        if (refreshTokenRepository.existsByUserIdAndToken(userId, providedToken)) {
            refreshTokenRepository.deleteByUserIdAndToken(userId, providedToken);
        }
    }

    // 전체 로그아웃 (모든 기기)
    public void invalidateAll(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    // Access Token 블랙리스트 등록
    public void blacklistAccessToken(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            return; // 이미 만료된 토큰은 블랙리스트에 넣지 않음
        }

        long expiration = jwtTokenProvider.getExpiration(accessToken); // 만료 시각 (ms)
        long now = System.currentTimeMillis();
        long ttl = expiration - now;

        if (ttl > 0) {
            String key = "blacklist:access:" + accessToken;
            redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.MILLISECONDS);
        }
    }

    // 🔹 Access Token 블랙리스트 확인
    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = "blacklist:access:" + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
