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

    // 특정 유저의 토큰 메타 데이터를 저장할 key 이름 - RefreshMeta를 value로서 저장
    private String tokenKey(Long userId, String jti) { return "refresh:uid:" + userId + ":token:" + jti; }
    // 특정 유저의 특정 토큰 해시를 저장할 key 이름 - jti를 value로서 저장
    private String hashKey(Long userId, String hash) { return "refresh:uid:" + userId + ":hash:" + hash; }
    // 특정 유저가 가진 모든 토큰들을 저장할 key 이름 - 특정 user의 모든 토큰 해시들을 저장
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

    /** TTL(sec)을 만료 시각과 정합성 있게 보정 (만료 초과 금지) */
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
            log.warn("[유효하지 않은 refresh_token] userId= {}", userId);
            return;
        }

        String jti  = jwtTokenProvider.getJti(c);

        // 만료 시간 계산
        long expMs  = jwtTokenProvider.getExpirationMillis(c);
        String hash = sha256(refreshTokenRaw);

        log.info("[refresh_token 저장 완료] uid={} jti={} expMs={} reqTtlSec={}",
                userId, jti, expMs, (ttl == null ? null : ttl.getSeconds()));

        if (userId == null || jti == null || jti.isBlank() || expMs <= 0) {
            log.warn("[입력 인자 누락] uid={} jti={} expMs={}", userId, jti, expMs);
            return;
        }

        long ttlSec = alignedTtlSec(ttl, expMs);
        if (ttlSec <= 0) {
            log.warn("[계산된 ttl이 0 이하임] uid={} jti={} expMs={}", userId, jti, expMs);
            return;
        }

        RefreshMeta meta = new RefreshMeta(userId, expMs / 1000, "active", null, null, c.get("fam", String.class));
        final String json;
        try {
            json = objectMapper.writeValueAsString(meta);
        } catch (Exception e) {
            throw new IllegalStateException("refresh_token 직렬화 실패", e);
        }

        // value를 저장할 key값들을 찾음
        final String keyToken  = tokenKey(userId, jti);
        final String keyHash   = hashKey(userId, hash);
        final String keyHashes = hashSet(userId);

        try {
            var exec = redisTemplate.execute(new SessionCallback<java.util.List<Object>>() {
                @Override
                @SuppressWarnings("unchecked")
                public java.util.List<Object> execute(RedisOperations operations) throws DataAccessException {
                    RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;
                    // 트랜잭션 시작 (명령어들 큐잉)
                    ops.multi();
                    // keyToken에 메타 데이터 저장
                    ops.opsForValue().set(keyToken, json, Duration.ofSeconds(ttlSec));
                    // keyHash에 jti 값 저장
                    ops.opsForValue().set(keyHash, jti, Duration.ofSeconds(ttlSec));
                    // keyHashes set에 토큰 해시 추가
                    ops.opsForSet().add(keyHashes, hash);

                    // 큐에 넣어둔 명령어들 한꺼번에 실행 (저장의 원자성 보장)
                    return ops.exec();
                }
            });
            log.info("refresh_token 저장 성공. EXEC 결과={}", exec);
        } catch (Exception e) {
            log.error("refresh_token 저장 실패", e);
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
            log.warn("refresh_token 존재 확인 중 에러 발생: {}", e.getMessage());
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
                ops.opsForSet().remove(keyHashes, refreshTokenHash);     // keyHashes 목록에서 이 hashToken 제거
                ops.exec();
                return null;
            }
        });
        log.info("refresh_token 하나 삭제 완료 uid={} jti={}", userId, jti);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        if (userId == null) return;

        final String keyHashes = hashSet(userId);

        // 해당 user의 토큰 해시들을 모두 모음 == 삭제해야할 토큰 해시들 모음
        Set<String> hashes = redisTemplate.opsForSet().members(keyHashes);


        java.util.List<String> keysToDelete = new java.util.ArrayList<>();
        if (hashes != null && !hashes.isEmpty()) {
            for (String h : hashes) {
                // keyHash들의 jti 값을 가져옴
                String jti = redisTemplate.opsForValue().get(hashKey(userId, h));
                if (jti != null && !jti.isBlank()) {
                    // tokenKey의 메타 데이터 삭제를 위해 리스트업
                    keysToDelete.add(tokenKey(userId, jti));
                }
                // hashKey의 jti 삭제를 위해 리스트업
                keysToDelete.add(hashKey(userId, h)); // 해시 매핑 키
            }
        }
        // keyHashes 삭제를 위해 리스트업
        keysToDelete.add(keyHashes);

        // keysToDelete에는 삭제해야할 모든 key값들이 저장돼있음
        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            @SuppressWarnings("unchecked")
            public Void execute(RedisOperations operations) throws DataAccessException {
                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;
                ops.multi();
                for (String k : keysToDelete) {
                    // 삭제해야할 모든 키들 다 삭제
                    ops.delete(k);
                }
                ops.exec();
                return null;
            }
        });
        log.info("uid={} 에 대한 모든 refresh_token /매핑/세트 키 삭제 완료. 삭제건수={}", userId, keysToDelete.size());
    }
}
