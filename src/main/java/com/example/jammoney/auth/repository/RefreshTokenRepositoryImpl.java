package com.example.jammoney.auth.repository;

import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    // ── Keys ──────────────────────────────────────────────────────────────────
    private String tokenKey(Long userId, String jti) { return "refresh:uid:" + userId + ":token:" + jti; }
    private String hashKey(Long userId, String hash) { return "refresh:uid:" + userId + ":hash:" + hash; }
    private String hashSet(Long userId)             { return "refresh:uid:" + userId + ":hashes"; }

    // ── Meta DTO ───────────────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor
    static class RefreshMeta {
        private Long userId;
        private Long expEpochSec; // 정보용(만료 판단은 TTL로 충분)
        private String status;    // "active" / "revoked"
        private String ua;        // optional
        private String ip;        // optional
        private String familyId;  // optional
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

    /** TTL(sec)을 만료시각과 정합성 있게 보정 (만료 초과 금지) */
    private long alignedTtlSec(Duration requestedTtl, long expMs) {
        if (expMs <= 0) return 0L;
        long nowMs = System.currentTimeMillis();
        long remainMs = Math.max(0, expMs - nowMs);
        long remainSec = remainMs / 1000;
        long req = (requestedTtl == null) ? remainSec : Math.max(0, requestedTtl.getSeconds());
        return Math.min(req, remainSec);
    }

    @Override
    public void saveByUserId(Long userId, String refreshTokenRaw, Duration ttl) {
        Claims c = jwtTokenProvider.parseClaims(refreshTokenRaw);
        if (c == null || !jwtTokenProvider.isRefreshToken(c)) {
            log.warn("[RT-SAVE] invalid token (null/not refresh). uid={}", userId);
            return;
        }

        String jti  = jwtTokenProvider.getJti(c);
        long expMs  = jwtTokenProvider.getExpirationMillis(c); // Claims 기반
        String hash = sha256(refreshTokenRaw);

        log.info("[RT-SAVE] uid={} jti={} expMs={} reqTtlSec={}",
                userId, jti, expMs, (ttl == null ? null : ttl.getSeconds()));

        if (userId == null || jti == null || jti.isBlank() || expMs <= 0) {
            log.warn("[RT-SAVE] invalid args uid={} jti={} expMs={}", userId, jti, expMs);
            return;
        }

        long ttlSec = alignedTtlSec(ttl, expMs);
        if (ttlSec <= 0) {
            log.warn("[RT-SAVE] ttlSec <= 0; skip save. uid={} jti={} expMs={}", userId, jti, expMs);
            return;
        }

        RefreshMeta meta = new RefreshMeta(userId, expMs / 1000, "active", null, null, c.get("fam", String.class));
        final String json;
        try {
            json = objectMapper.writeValueAsString(meta);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize refresh meta", e);
        }

        final String keyToken  = tokenKey(userId, jti);
        final String keyHash   = hashKey(userId, hash);
        final String keyHashes = hashSet(userId);

        try {
            var exec = redisTemplate.execute(new SessionCallback<java.util.List<Object>>() {
                @Override
                @SuppressWarnings("unchecked")
                public java.util.List<Object> execute(RedisOperations operations) throws DataAccessException {
                    RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;
                    ops.multi();
                    // 메타(JSON) - TTL
                    ops.opsForValue().set(keyToken, json, Duration.ofSeconds(ttlSec));
                    // 해시→jti 매핑 - TTL
                    ops.opsForValue().set(keyHash, jti, Duration.ofSeconds(ttlSec));
                    // 일괄 삭제용 해시 목록(Set) — 멤버 TTL 없음, 전체 삭제 시 세트 자체 제거
                    ops.opsForSet().add(keyHashes, hash);
                    return ops.exec();
                }
            });
            log.info("[RT-SAVE] EXEC results={}", exec);
        } catch (Exception e) {
            log.error("[RT-SAVE] EXEC failed", e);
        }
    }

    @Override
    public boolean existsByUserIdAndHash(Long userId, String refreshTokenHash) {
        try {
            if (userId == null || refreshTokenHash == null || refreshTokenHash.isBlank()) return false;

            String jti = redisTemplate.opsForValue().get(hashKey(userId, refreshTokenHash));
            if (jti == null || jti.isBlank()) return false;

            String json = redisTemplate.opsForValue().get(tokenKey(userId, jti));
            if (json == null) return false;

            RefreshMeta meta = objectMapper.readValue(json, RefreshMeta.class);
            if (!Objects.equals(meta.getUserId(), userId)) return false;
            if (!"active".equalsIgnoreCase(meta.getStatus())) return false;

            return true;
        } catch (Exception e) {
            log.warn("[RT-EXISTS] error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteByUserIdAndHash(Long userId, String refreshTokenHash) {
        if (userId == null || refreshTokenHash == null || refreshTokenHash.isBlank()) return;

        String jti = redisTemplate.opsForValue().get(hashKey(userId, refreshTokenHash));
        final String keyToken  = (jti == null ? null : tokenKey(userId, jti));
        final String keyHash   = hashKey(userId, refreshTokenHash);
        final String keyHashes = hashSet(userId);

        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            @SuppressWarnings("unchecked")
            public Void execute(RedisOperations operations) throws DataAccessException {
                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;
                ops.multi();
                if (keyToken != null) ops.delete(keyToken);             // 메타 삭제(존재 시)
                ops.delete(keyHash);                                     // 해시 매핑 삭제
                ops.opsForSet().remove(keyHashes, refreshTokenHash);     // 세트 멤버 제거
                ops.exec();
                return null;
            }
        });
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        if (userId == null) return;

        final String keyHashes = hashSet(userId);
        Set<String> hashes = redisTemplate.opsForSet().members(keyHashes);

        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            @SuppressWarnings("unchecked")
            public Void execute(RedisOperations operations) throws DataAccessException {
                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;
                ops.multi();

                if (hashes != null) {
                    for (String h : hashes) {
                        String jti = ops.opsForValue().get(hashKey(userId, h)); // 같은 커넥션 사용
                        if (jti != null && !jti.isBlank()) {
                            ops.delete(tokenKey(userId, jti));
                        }
                        ops.delete(hashKey(userId, h));
                    }
                }
                // 세트 자체 삭제로 찌꺼기 멤버 문제 정리
                ops.delete(keyHashes);

                ops.exec();
                return null;
            }
        });
    }
}
