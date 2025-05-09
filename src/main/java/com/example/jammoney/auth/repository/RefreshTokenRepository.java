package com.example.jammoney.auth.repository;

import com.example.jammoney.auth.entity.RefreshToken;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByUserEmail(String email);

}