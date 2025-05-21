package com.example.jammoney.auth.service;

import com.example.jammoney.auth.entity.RefreshToken;
import com.example.jammoney.auth.repository.RefreshTokenRepository;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    public RefreshToken createRefreshToken(User user) {
        // 기존 토큰 삭제 후 flush
        refreshTokenRepository.findByUser(user).ifPresent(token -> {
            refreshTokenRepository.delete(token);
            refreshTokenRepository.flush();
        });

        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusSeconds(refreshTokenValidityInSeconds);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiry)
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
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByEmail(String email) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByUserEmail(email);
        tokenOpt.ifPresent(refreshTokenRepository::delete);
    }
}