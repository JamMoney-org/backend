package com.example.jammoney.auth.service;

import com.example.jammoney.auth.entity.RefreshToken;
import com.example.jammoney.auth.repository.RefreshTokenRepository;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    public String createRefreshToken(String email) {
        String token = UUID.randomUUID().toString();
        String key = "RT:" + email;

        redisTemplate.opsForValue().set(key, token, refreshTokenValidityInSeconds, TimeUnit.SECONDS);
        return token;
    }
    public Optional<String> findByEmail(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get("RT:" + email));
    }

    public boolean validate(String email, String token) {
        return findByEmail(email)
                .map(stored -> stored.equals(token))
                .orElse(false);
    }

    public void deleteByEmail(String email) {
        redisTemplate.delete("RT:" + email);
    }
}

