package com.example.jammoney.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // 블랙리스트 등록
    public void blacklistAccessToken(String accessToken, long ttlMillis) {
        String key = "BL:" + accessToken;
        redisTemplate.opsForValue().set(key, "logout", ttlMillis, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트 확인
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("BL:" + accessToken));
    }
}
