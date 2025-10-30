package com.example.jammoney.auth.repository;

import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public interface RefreshTokenRepository {

    void saveByUserId(Long userId, String refreshTokenRaw, Duration ttl);

    boolean existsByUserIdAndHash(Long userId, String hash);

    void deleteByUserIdAndHash(Long userId, String hash);

    void deleteAllByUserId(Long userId);
}
