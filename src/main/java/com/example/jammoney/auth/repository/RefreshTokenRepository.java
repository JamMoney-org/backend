package com.example.jammoney.auth.repository;

import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public interface RefreshTokenRepository {

    /**
     * 새로운 Refresh Token 저장
     * - 입력은 "원본 토큰(raw)"이어야 함 (jti/exp 추출용)
     * - 내부적으로 hash→jti 매핑과 메타(JSON)를 함께 저장
     * - ZSET 사용 안 함 (TTL로 만료 관리)
     */
    void saveByUserId(Long userId, String refreshTokenRaw, Duration ttl);

    /**
     * 특정 userId의 토큰 집합 중
     * 주어진 "토큰 해시"가 존재(active)하는지 확인
     * - 존재 판단은 메타 키(token:{jti})의 TTL/존재 여부로 충분 (만료되면 자동 삭제됨)
     */
    boolean existsByUserIdAndHash(Long userId, String refreshTokenHash);

    /**
     * 특정 userId의 특정 "토큰 해시"만 제거 (단일 기기 로그아웃)
     */
    void deleteByUserIdAndHash(Long userId, String refreshTokenHash);

    /**
     * 특정 userId의 모든 Refresh Token 제거 (전체 로그아웃)
     */
    void deleteAllByUserId(Long userId);
}
