package com.example.jammoney.auth.jwt;

import com.example.jammoney.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessValidity;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshValidity;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** -------------------- 토큰 생성 -------------------- **/

    public String generateAccessToken(String email) {
        return generateToken(email, accessValidity);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, refreshValidity);
    }

    private String generateToken(String email, long validityInSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInSeconds * 1000);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** -------------------- 토큰 파싱 및 검증 -------------------- **/

    public String getEmailFromToken(String token) {
        return parseAllClaims(token).getSubject();
    }

    public Date getExpirationFromToken(String token) {
        return parseAllClaims(token).getExpiration();
    }

    public long getRemainingTime(String token) {
        return getExpirationFromToken(token).getTime() - System.currentTimeMillis();
    }

    public boolean validateToken(String token) {
        try {
            parseAllClaims(token); // 내부적으로 유효성 검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** -------------------- 인증 객체 생성 -------------------- **/

    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
