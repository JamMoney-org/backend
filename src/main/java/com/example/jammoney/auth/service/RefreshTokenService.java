package com.example.jammoney.auth.service;

import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.repository.RefreshTokenRepository;
import com.example.jammoney.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    private static final Duration REFRESH_TOKEN_TTL = null;

    /** 로그인 시 refresh 저장 (해시 저장) */
    public void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenRepository.saveByUserId(userId, refreshToken, REFRESH_TOKEN_TTL);
    }

    /** 제공된 refresh 토큰이 유효하고 내 저장소와 일치하는지 검증 (해시 비교) */
    public void assertTokenValid(Long userId, String providedToken) {
        if (providedToken == null || providedToken.isBlank()) throw new InvalidRefreshTokenException();
        if (!jwtTokenProvider.validate(providedToken)) throw new InvalidRefreshTokenException();
        String hash = sha256(providedToken);
        if (!refreshTokenRepository.existsByUserIdAndHash(userId, hash)) {
            throw new InvalidRefreshTokenException();
        }
    }

    /** refresh 토큰 회전 — family 유지, 해시 저장 */
    public String reissueRefreshToken(Long userId, String oldToken) {
        // 1) 유효성/정합성 확인 (내부에서 existsByUserIdAndHash 사용)
        assertTokenValid(userId, oldToken);

        // 2) 기존 토큰 폐기 (해시 기준)
        String oldHash = sha256(oldToken);
        refreshTokenRepository.deleteByUserIdAndHash(userId, oldHash);

        // 3) 새 토큰 발급 (family 유지)
        String username = jwtTokenProvider.getUsername(oldToken);
        String familyId = jwtTokenProvider.getFamilyId(oldToken);
        String newToken = jwtTokenProvider.generateRefreshToken(userId, username, familyId);

        // 4) 저장 (raw 전달 → 내부에서 jti/exp 추출 + hash 매핑)
        saveRefreshToken(userId, newToken);

        return newToken;
    }

    /** 단일 기기 로그아웃 */
    public void invalidateOne(Long userId, String providedToken) {
        if (providedToken == null || providedToken.isBlank()) return;
        String hash = sha256(providedToken);
        refreshTokenRepository.deleteByUserIdAndHash(userId, hash);
    }

    /** 전체 로그아웃 */
    public void invalidateAll(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    /** Access Token 블랙리스트 등록 (남은 TTL) */
    public void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return;
        if (!jwtTokenProvider.validate(accessToken)) return;

        long expMs = jwtTokenProvider.getExpiration(accessToken);
        long nowMs = System.currentTimeMillis();
        long ttlMs = expMs - nowMs;
        if (ttlMs <= 0) return;

        String key = "blacklist:access:" + sha256(accessToken);
        redisTemplate.opsForValue().set(key, "1", ttlMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /** Access Token 블랙리스트 확인 */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return false;
        String key = "blacklist:access:" + sha256(accessToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private static String sha256(String in) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(in.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
