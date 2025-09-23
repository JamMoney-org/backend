package com.example.jammoney.auth.repository;

import java.time.Duration;

public interface RefreshTokenRepository {

    /**
     * 새로운 Refresh Token 저장
     * 여러 기기 동시 로그인 허용 → 같은 userId로 여러 토큰 저장 가능
     */
    void saveByUserId(Long userId, String refreshToken, Duration ttl);

    /**
     * 특정 userId가 가진 Refresh Token 집합 안에
     * 주어진 refreshToken이 존재하는지 확인
     */
    boolean existsByUserIdAndToken(Long userId, String refreshToken);

    /**
     * 특정 userId의 특정 Refresh Token만 제거 (단일 기기 로그아웃)
     */
    void deleteByUserIdAndToken(Long userId, String refreshToken);

    /**
     * 특정 userId의 모든 Refresh Token 제거 (전체 로그아웃)
     */
    void deleteAllByUserId(Long userId);
}
