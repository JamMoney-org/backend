package com.example.jammoney.auth.service;

import com.example.jammoney.auth.entity.RefreshToken;
import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.repository.RefreshTokenRepository;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    public RefreshToken createRefreshToken(User user) {
        // 기존 토큰 삭제
        refreshTokenRepository.findByUser(user).ifPresent(token -> {
            refreshTokenRepository.delete(token);
            refreshTokenRepository.flush();
        });

        // JWT 기반 refreshToken 생성
        String token = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // JWT 토큰에도 만료시간이 있지만, DB에도 따로 expiryDate 저장
        Date jwtExpiry = jwtTokenProvider.getExpirationFromToken(token); // 아래 참고

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(jwtExpiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public boolean validate(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> !rt.isExpired())
                .orElse(false);
    }

    public User getUserByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::getUser)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 리프레시 토큰입니다."));
    }

    public Optional<RefreshToken> findByToken(String token) {
        log.info("DB에서 찾은 refreshToken: {}", refreshTokenRepository.findByToken(token));
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByEmail(String email) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByUserEmail(email);
        tokenOpt.ifPresent(refreshTokenRepository::delete);
    }
}