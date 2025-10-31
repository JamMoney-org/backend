package com.example.jammoney.auth.service;

import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.repository.RefreshTokenRepository;
import com.example.jammoney.exception.InvalidRefreshTokenException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    /** 로그인 시 refresh 저장 (해시 저장) — 토큰 exp 기준 TTL 반영 */
    public void saveRefreshToken(Long userId, String refreshToken) {
        Claims c = jwtTokenProvider.parseClaims(refreshToken);
        if (c == null || !jwtTokenProvider.isRefreshToken(c)) {
            throw new InvalidRefreshTokenException();
        }
        // 남은 시간 계산
        Duration ttl = calculateRemainingTime(c);
        refreshTokenRepository.saveByUserId(userId, refreshToken, ttl);
    }

    /** 제공된 refresh 토큰이 유효하고 내 저장소와 일치하는지 검증 (해시 비교) */
    public void assertTokenValid(Long userId, String providedToken) {
        if (providedToken == null || providedToken.isBlank()) throw new InvalidRefreshTokenException();

        Claims c = jwtTokenProvider.parseClaims(providedToken);
        if (c == null || !jwtTokenProvider.isRefreshToken(c)) throw new InvalidRefreshTokenException();

        // 토큰의 uid와 인자로 받은 userId가 불일치하면 거절
        Long uidFromToken = jwtTokenProvider.getUserId(c);
        if (uidFromToken == null || !uidFromToken.equals(userId)) throw new InvalidRefreshTokenException();

        // 저장소에 존재하는 해시인지 확인
        String hash = sha256(providedToken);
        if (!refreshTokenRepository.existsByUserIdAndHash(userId, hash)) {
            throw new InvalidRefreshTokenException();
        }
    }

    /** refresh 토큰 회전 — family 유지, 해시 저장 (exp/TTL 동기화) */
    public String reissueRefreshToken(Long userId, String oldToken) {
        // 1) 유효성/정합성 확인
        assertTokenValid(userId, oldToken);

        // 2) 기존 토큰 해시 삭제
        String oldHash = sha256(oldToken);
        refreshTokenRepository.deleteByUserIdAndHash(userId, oldHash);

        // 3) 새 토큰 발급
        Claims oldC = jwtTokenProvider.parseClaims(oldToken);
        String username = jwtTokenProvider.getUsername(oldC);
        String familyId = jwtTokenProvider.getFamilyId(oldC);
        // 기존 토큰의 family_id를 그대로 가져와서 family_id로 등록
        String newToken = jwtTokenProvider.generateRefreshToken(userId, username, familyId);

        // 4) 저장 (exp 기반 TTL 반영)
        saveRefreshToken(userId, newToken);

        return newToken;
    }

    /** 단일 기기 로그아웃 */
    public void invalidateOne(Long userId, String providedToken) {
        if (providedToken == null || providedToken.isBlank()) return;
        String hash = sha256(providedToken);
        // 레디스에서 refresh_token 삭제
        refreshTokenRepository.deleteByUserIdAndHash(userId, hash);
    }

    /** 전체 로그아웃 */
    public void invalidateAll(Long userId) {
        // 레디스에서 같은 user_id를 가지는 모든 refresh_token 삭제
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    /** Access Token 블랙리스트 등록 (남은 TTL = exp-now) */
    public void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return;

        // 클레임 파싱
        Claims c = jwtTokenProvider.parseClaims(accessToken);
        if (c == null) return; // 무효/만료 등
        long remainSec = jwtTokenProvider.getRemainingSeconds(c);
        if (remainSec <= 0) return;

        String key = "blacklist:access:" + sha256(accessToken);
        // 토큰의 남은 만료 시간 뒤에는 자동으로 블랙리스트 목록에서 해당 토큰이 삭제됨
        redisTemplate.opsForValue().set(key, "1", remainSec, TimeUnit.SECONDS);
    }

    /** Access Token 블랙리스트 확인 */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return false;
        String key = "blacklist:access:" + sha256(accessToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Duration calculateRemainingTime(Claims c) {
        long remainSec = Math.max(1, jwtTokenProvider.getRemainingSeconds(c)); // 최소 1초
        return Duration.ofSeconds(remainSec);
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
