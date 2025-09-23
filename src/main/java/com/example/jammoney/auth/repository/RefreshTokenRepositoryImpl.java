package com.example.jammoney.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_BY_USER_PREFIX = "refresh:user:";

    @Override
    public void saveByUserId(Long userId, String refreshToken, Duration ttl) {
        String key = REFRESH_BY_USER_PREFIX + userId;
        redisTemplate.opsForSet().add(key, refreshToken);
        redisTemplate.expire(key, ttl); // TTL 갱신
    }

    @Override
    public boolean existsByUserIdAndToken(Long userId, String refreshToken) {
        String key = REFRESH_BY_USER_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, refreshToken));
    }

    @Override
    public void deleteByUserIdAndToken(Long userId, String refreshToken) {
        String key = REFRESH_BY_USER_PREFIX + userId;
        redisTemplate.opsForSet().remove(key, refreshToken);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        String key = REFRESH_BY_USER_PREFIX + userId;
        redisTemplate.delete(key);
    }
}



